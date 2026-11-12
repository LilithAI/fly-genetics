package io.flyproject.connectomefly.neural;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.flyproject.connectomefly.genetics.Genome;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public final class WholeBrain {
   private static final float TAU_M = 20.0F;
   private static final float V_TH = 1.0F;
   private static final float V_RESET = 0.0F;
   private static final float T_REFRAC = 2.0F;
   private static final float TAU_SYN = 5.0F;
   private static final float DRIVE_RATE = 0.001F;
   private static final float DRIVE_AMP = 10.0F;
   private static final float DN_DRIVE_RATE = 0.004F;
   private static final float SYN_MULT = 2.2F;
   private static final float SENS_MULT = 4.0F;
   private static final float INH_MULT = 1.1F;
   private static final float ADAPT_INC = 0.4F;
   private static final float ADAPT_TAU = 250.0F;
   private static final boolean ADAPT_SENS = false;
   private static final float BUS_RATE = 0.05F;
   private static final float DT = 1.0F;
   private static final float DECAY = (float)Math.exp(-0.2F);
   private static final float K = 0.05F;
   private static final float ADECAY = (float)Math.exp(-0.004);
   private static final int RDEPTH = Math.max(1, Math.round(2.0F));
   private static final int MAX_STEPS_PER_LOOP = 120;
   private static final long REPORT_MS = 66L;
   private static volatile WholeBrain.Data shared;
   private static final Object LOAD_LOCK = new Object();
   private static final AtomicInteger RUNNING = new AtomicInteger();
   private WholeBrain.Data d;
   private float[] v;
   private float[] isyn;
   private float[] a;
   private int[] spk;
   private int[][] refBuf;
   private int[] refN;
   private int refCur;
   private int[] chanSpikeAcc;
   private int[] outSpikeAcc;
   private float[] outBase;
   private boolean[] outActive;
   private double epgCosAcc;
   private double epgSinAcc;
   private long epgAcc;
   private float[] plastScale;
   private float[] kcTrace;
   private int[] kcList;
   private float pamLP;
   private float ppl1LP;
   private float pamBase = -1.0F;
   private float ppl1Base = -1.0F;
   private int plastTick;
   private int pamOut = -1;
   private int ppl1Out = -1;
   private static final float TRACE_DECAY = (float)Math.exp(-6.666666666666666E-4);
   private static final float ETA = 0.06F;
   private static final float FORGET = 2.0E-5F;
   private static final float PAM_FLOOR = 0.4F;
   private static final float PAM_SCALE = 2.0F;
   private static final float PPL1_FLOOR = 0.04F;
   private static final float PPL1_SCALE = 0.12F;
   private final SplittableRandom rng = new SplittableRandom();
   private volatile float[] bus = new float[0];
   private volatile float compassHeading = Float.NaN;
   private volatile float compassStrength;
   private float[] epgDrive;
   private volatile WholeBrain.Loco loco;
   private volatile boolean running;
   private volatile long lastKeepAlive = System.nanoTime();
   private Thread thread;
   private final String label;
   // [model] genetics: per-fly tuning. Defaults are the author's constants; a Genome multiplies them.
   private final Genome genome;
   private final float gDecay;
   private final float gADecay;
   private final float gK;
   private final float gVth;
   private final float gDriveRate;
   private final float gDriveAmp;
   private final float gDnDriveRate;
   private final float gSyn;
   private final float gSens;
   private final float gInh;
   private final float gAdaptInc;
   private final float gBusRate;
   private final float gEta;
   private final float gForget;
   private float[] chanGain;
   private float[] maskMul;
   private boolean groupsActive;
   private volatile float[] initialPlast;

   public static int running() {
      return RUNNING.get();
   }

   public static WholeBrain.Data data() throws IOException {
      WholeBrain.Data d = shared;
      if (d != null) {
         return d;
      } else {
         synchronized (LOAD_LOCK) {
            if (shared != null) {
               return shared;
            } else {
               long t0 = System.currentTimeMillis();

               byte[] raw;
               try (InputStream in = WholeBrain.class.getResourceAsStream("/data/connectomefly/fullbrain.bin.gz")) {
                  if (in == null) {
                     throw new IOException("fullbrain.bin.gz missing from the mod");
                  }

                  try (GZIPInputStream gz = new GZIPInputStream(in, 65536)) {
                     ByteArrayOutputStream bo = new ByteArrayOutputStream(27262976);
                     byte[] buf = new byte[65536];

                     int r;
                     while ((r = gz.read(buf)) > 0) {
                        bo.write(buf, 0, r);
                     }

                     raw = bo.toByteArray();
                  }
               }

               ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
               int n = bb.getInt();
               int nEdges = bb.getInt();
               int[] indptr = new int[n + 1];
               bb.asIntBuffer().get(indptr);
               bb.position(bb.position() + 4 * (n + 1));
               int[] indices = new int[nEdges];
               bb.asIntBuffer().get(indices);
               bb.position(bb.position() + 4 * nEdges);
               float[] weight = new float[nEdges];
               bb.asFloatBuffer().get(weight);
               bb.position(bb.position() + 4 * nEdges);
               byte[] sensory = new byte[n];
               bb.get(sensory);
               bb.position(bb.position() + (-n % 4 + 4) % 4);
               byte[] dnclass = new byte[n];
               bb.get(dnclass);
               String[] names = new String[0];
               int[][] idx = new int[0][];

               try (InputStream in = WholeBrain.class.getResourceAsStream("/data/connectomefly/brain_channels.json")) {
                  if (in != null) {
                     JsonObject j = (JsonObject)new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
                     names = j.keySet().toArray(new String[0]);
                     idx = new int[names.length][];

                     for (int c = 0; c < names.length; c++) {
                        JsonArray a = j.getAsJsonArray(names[c]);
                        idx[c] = new int[a.size()];

                        for (int k = 0; k < a.size(); k++) {
                           idx[c][k] = a.get(k).getAsInt();
                        }
                     }
                  }
               }

               WholeBrain.Data data = new WholeBrain.Data(n, nEdges, indptr, indices, weight, sensory, dnclass, names, idx);

               try (InputStream inx = WholeBrain.class.getResourceAsStream("/data/connectomefly/brain_outputs.json")) {
                  if (inx != null) {
                     JsonObject j = (JsonObject)new Gson().fromJson(new InputStreamReader(inx, StandardCharsets.UTF_8), JsonObject.class);
                     List<String> nm = new ArrayList<>(j.keySet());
                     if (nm.size() > 63) {
                        nm = nm.subList(0, 63);
                     }

                     data.outNames = nm.toArray(new String[0]);
                     data.outLabels = new String[nm.size()];
                     data.outIdx = new int[nm.size()][];
                     data.neuronOutMask = new long[n];

                     for (int o = 0; o < nm.size(); o++) {
                        JsonObject pop = j.getAsJsonObject(nm.get(o));
                        data.outLabels[o] = pop.get("label").getAsString();
                        JsonArray a = pop.getAsJsonArray("idx");
                        data.outIdx[o] = new int[a.size()];

                        for (int k = 0; k < a.size(); k++) {
                           int i = a.get(k).getAsInt();
                           data.outIdx[o][k] = i;
                           if (i >= 0 && i < n) {
                              data.neuronOutMask[i] = data.neuronOutMask[i] | 1L << o;
                           }
                        }
                     }
                  }
               }

               try (InputStream inxx = WholeBrain.class.getResourceAsStream("/data/connectomefly/brain_circuit_gains.json")) {
                  if (inxx != null) {
                     JsonObject j = (JsonObject)new Gson().fromJson(new InputStreamReader(inxx, StandardCharsets.UTF_8), JsonObject.class);
                     StringBuilder note = new StringBuilder();

                     for (String name : j.keySet()) {
                        JsonObject c = j.getAsJsonObject(name);
                        float gain = c.get("gain").getAsFloat();
                        String ov = System.getProperty("connectomefly.gain." + name);
                        if (ov != null) {
                           gain = Float.parseFloat(ov);
                        }

                        {
                           boolean[] src = new boolean[n];
                           boolean[] dst = new boolean[n];

                           for (JsonElement v : c.getAsJsonArray("src")) {
                              int i = v.getAsInt();
                              if (i >= 0 && i < n) {
                                 src[i] = true;
                              }
                           }

                           for (JsonElement vx : c.getAsJsonArray("dst")) {
                              int i = vx.getAsInt();
                              if (i >= 0 && i < n) {
                                 dst[i] = true;
                              }
                           }

                           if (data.edgeGain == null) {
                              data.edgeGain = new float[nEdges];
                              Arrays.fill(data.edgeGain, 1.0F);
                           }

                           int ne = 0;
                           int gi = data.groupNames.length;
                           if (data.edgeGroups == null) {
                              data.edgeGroups = new byte[nEdges];
                           }

                           if (gi < 8) {
                              data.groupNames = Arrays.copyOf(data.groupNames, gi + 1);
                              data.groupNames[gi] = name;
                           }

                           for (int i = 0; i < n; i++) {
                              if (src[i]) {
                                 for (int q = indptr[i]; q < indptr[i + 1]; q++) {
                                    if (dst[indices[q]]) {
                                       data.edgeGain[q] = data.edgeGain[q] * gain;
                                       if (gi < 8) {
                                          data.edgeGroups[q] = (byte)(data.edgeGroups[q] | 1 << gi);
                                       }

                                       ne++;
                                    }
                                 }
                              }
                           }

                           if (gain != 1.0F) note.append(name).append(" x").append(gain).append(" (").append(ne).append(" synapses); ");
                        }
                     }

                     data.circuitGainNote = note.toString();
                     if (data.edgeGain != null) {
                        for (int qx = 0; qx < nEdges; qx++) {
                           weight[qx] *= data.edgeGain[qx];
                        }
                     }

                     System.out.println("[connectomefly] circuit gains: " + (note.length() == 0 ? "none" : note));
                  }
               }

               try (InputStream inxxx = WholeBrain.class.getResourceAsStream("/data/connectomefly/brain_compass.json")) {
                  if (inxxx != null) {
                     JsonObject j = (JsonObject)new Gson().fromJson(new InputStreamReader(inxxx, StandardCharsets.UTF_8), JsonObject.class);
                     JsonArray e = j.getAsJsonArray("epg");
                     JsonArray ang = j.getAsJsonArray("angle");
                     data.epgIdx = new int[e.size()];
                     data.epgAngle = new float[n];
                     Arrays.fill(data.epgAngle, Float.NaN);

                     for (int kx = 0; kx < e.size(); kx++) {
                        int ix = e.get(kx).getAsInt();
                        data.epgIdx[kx] = ix;
                        if (ix >= 0 && ix < n) {
                           data.epgAngle[ix] = ang.get(kx).getAsFloat();
                        }
                     }

                     data.isKC = new boolean[n];

                     for (JsonElement vxx : j.getAsJsonArray("kc")) {
                        int ix = vxx.getAsInt();
                        if (ix >= 0 && ix < n) {
                           data.isKC[ix] = true;
                        }
                     }

                     boolean[] isMbon = new boolean[n];

                     for (JsonElement vxxx : j.getAsJsonArray("mbon")) {
                        int ix = vxxx.getAsInt();
                        if (ix >= 0 && ix < n) {
                           isMbon[ix] = true;
                        }
                     }

                     data.mbonClass = new byte[n];
                     int avoid = data.out("mbon_avoid");
                     int appr = data.out("mbon_approach");
                     if (avoid >= 0) {
                        for (int ix : data.outIdx[avoid]) {
                           data.mbonClass[ix] = 1;
                        }
                     }

                     if (appr >= 0) {
                        for (int ix : data.outIdx[appr]) {
                           data.mbonClass[ix] = 2;
                        }
                     }

                     data.plastPos = new int[nEdges];
                     Arrays.fill(data.plastPos, -1);
                     int np = 0;

                     for (int ix = 0; ix < n; ix++) {
                        if (data.isKC[ix]) {
                           for (int qx = indptr[ix]; qx < indptr[ix + 1]; qx++) {
                              if (isMbon[indices[qx]]) {
                                 data.plastPos[qx] = np++;
                              }
                           }
                        }
                     }

                     data.nPlastic = np;
                     data.plastTarget = new int[np];
                     data.plastSource = new int[np];

                     for (int ixx = 0; ixx < n; ixx++) {
                        if (data.isKC[ixx]) {
                           for (int qxx = indptr[ixx]; qxx < indptr[ixx + 1]; qxx++) {
                              int pp = data.plastPos[qxx];
                              if (pp >= 0) {
                                 data.plastTarget[pp] = indices[qxx];
                                 data.plastSource[pp] = ixx;
                              }
                           }
                        }
                     }

                     System.out.println("[connectomefly] compass: " + data.epgIdx.length + " EPGs; mushroom body: " + np + " plastic KC->MBON synapses");
                  }
               }

               shared = data;
               System.out
                  .println(
                     "[connectomefly] whole brain loaded: "
                        + n
                        + " neurons, "
                        + nEdges
                        + " synapses, "
                        + names.length
                        + " sensory channels, "
                        + (System.currentTimeMillis() - t0)
                        + " ms"
                  );
               return shared;
            }
         }
      }
   }

   public WholeBrain(String label) {
      this(label, null);
   }

   public WholeBrain(String label, Genome genome) {
      this.label = label;
      this.genome = genome;
      Genome q = genome == null ? Genome.baseline() : genome;
      this.gK = 1.0F / (TAU_M * q.get("tau_m"));
      this.gVth = V_TH * q.get("v_th");
      this.gDecay = (float)Math.exp(-1.0F / (TAU_SYN * q.get("tau_syn")));
      this.gDriveRate = DRIVE_RATE * q.get("drive_rate");
      this.gDriveAmp = DRIVE_AMP * q.get("drive_amp");
      this.gDnDriveRate = DN_DRIVE_RATE * q.get("dn_drive_rate");
      this.gSyn = SYN_MULT * q.get("syn_mult");
      this.gSens = SENS_MULT * q.get("sens_mult");
      this.gInh = INH_MULT * q.get("inh_mult");
      this.gAdaptInc = ADAPT_INC * q.get("adapt_inc");
      this.gADecay = (float)Math.exp(-1.0F / (ADAPT_TAU * q.get("adapt_tau")));
      this.gBusRate = BUS_RATE * q.get("bus_rate");
      this.gEta = q.get("eta");
      this.gForget = FORGET * q.get("forget");
   }

   public Genome genome() {
      return this.genome;
   }

   /** [model] not biology: start with a faint copy of the mother's learned weights. Call before start(). */
   public void setInitialMemory(float[] plast) {
      this.initialPlast = plast;
   }

   /** A copy of this fly's learned mushroom-body weights (1.0 = nothing learned), or null before the brain is up. */
   public float[] memorySnapshot() {
      float[] p = this.plastScale;
      return p == null ? null : (float[])p.clone();
   }

   public WholeBrain.Loco loco() {
      return this.loco;
   }

   public boolean ready() {
      return this.d != null && this.running;
   }

   public WholeBrain.Data dataOrNull() {
      return this.d;
   }

   public void keepAlive() {
      this.lastKeepAlive = System.nanoTime();
   }

   public void setBus(Map<String, Float> values) {
      WholeBrain.Data dd = this.d;
      if (dd != null) {
         float[] b = new float[dd.chanNames.length];

         for (int c = 0; c < b.length; c++) {
            Float f = values.get(dd.chanNames[c]);
            b[c] = f == null ? 0.0F : Math.max(0.0F, Math.min(1.0F, f));
         }

         this.bus = b;
      }
   }

   public void setCompassInput(float headingRad, float strength) {
      this.compassHeading = headingRad;
      this.compassStrength = Math.max(0.0F, Math.min(1.0F, strength));
   }

   public void start() {
      if (this.thread == null) {
         this.running = true;
         RUNNING.incrementAndGet();
         this.thread = new Thread(this::run, "connectome-fly brain " + this.label);
         this.thread.setDaemon(true);
         this.thread.setPriority(4);
         this.thread.start();
      }
   }

   public void stop() {
      this.running = false;
   }

   private void run() {
      try {
         this.d = data();
      } catch (Exception var36) {
         System.err.println("[connectomefly] whole brain failed to load: " + var36);
         this.running = false;
         RUNNING.decrementAndGet();
         return;
      }

      int n = this.d.n;
      this.v = new float[n];
      this.isyn = new float[n];
      this.a = new float[n];
      this.spk = new int[n];
      this.refBuf = new int[RDEPTH][n];
      this.refN = new int[RDEPTH];
      this.chanSpikeAcc = new int[this.d.chanNames.length];
      this.outSpikeAcc = new int[this.d.outNames.length];
      this.outBase = new float[this.d.outNames.length];
      this.outActive = new boolean[this.d.outNames.length];
      Arrays.fill(this.outBase, -1.0F);
      if (this.d.nPlastic > 0) {
         this.plastScale = new float[this.d.nPlastic];
         Arrays.fill(this.plastScale, 1.0F);
         this.kcTrace = new float[n];
         float[] ip = this.initialPlast;
         if (ip != null && ip.length == this.plastScale.length) {
            System.arraycopy(ip, 0, this.plastScale, 0, ip.length);
         }

         this.pamOut = this.d.out("pam");
         this.ppl1Out = this.d.out("ppl1");
      }

      this.chanGain = new float[this.d.chanNames.length];
      Arrays.fill(this.chanGain, 1.0F);
      if (this.genome != null) {
         for (int c = 0; c < this.chanGain.length; c++) {
            int gi = Genome.senseGeneFor(this.d.chanNames[c]);
            if (gi >= 0) {
               this.chanGain[c] = this.genome.g[gi];
            }
         }

         String[] gn = this.d.groupNames;
         this.maskMul = new float[1 << gn.length];

         for (int m = 0; m < this.maskMul.length; m++) {
            float x = 1.0F;

            for (int gi = 0; gi < gn.length; gi++) {
               if ((m & 1 << gi) != 0) {
                  x *= this.genome.get(gn[gi]);
               }
            }

            this.maskMul[m] = x;
            if (x != 1.0F) {
               this.groupsActive = true;
            }
         }
      }

      long lastWall = System.nanoTime();
      long lastPost = lastWall;
      double simCarry = 0.0;
      long dnL = 0L;
      long dnR = 0L;
      long accSteps = 0L;
      long spkAcc = 0L;

      while (this.running) {
         long now = System.nanoTime();
         if (now - this.lastKeepAlive > 5000000000L) {
            break;
         }

         simCarry += (now - lastWall) / 1000000.0;
         lastWall = now;
         int steps = (int)Math.floor(simCarry / 1.0);
         if (steps > 120) {
            steps = 120;
         }

         simCarry -= steps * 1.0F;
         float[] b = this.bus;
         long[] dn = new long[2];

         for (int s = 0; s < steps; s++) {
            spkAcc += this.step(b, dn);
         }

         dnL += dn[0];
         dnR += dn[1];
         accSteps += steps;
         if ((now - lastPost) / 1000000L >= 66L && accSteps > 0L) {
            long tot = dnL + dnR;
            float[] cs = new float[this.chanSpikeAcc.length];

            for (int c = 0; c < cs.length; c++) {
               cs[c] = this.chanSpikeAcc[c] / (float)accSteps;
               this.chanSpikeAcc[c] = 0;
            }

            WholeBrain.Loco l = new WholeBrain.Loco(
               tot > 0L ? (float)(dnR - dnL) / (float)(tot + 8L) : 0.0F,
               (float)tot / (float)accSteps,
               (float)spkAcc / (float)accSteps,
               cs,
               (float)accSteps * 1.0E9F / (float)(now - lastPost),
               (float)dnL / (float)accSteps,
               (float)dnR / (float)accSteps
            );
            float[] rates = new float[this.outSpikeAcc.length];
            float[] bases = new float[this.outSpikeAcc.length];

            for (int o = 0; o < this.outSpikeAcc.length; o++) {
               float r = this.outSpikeAcc[o] / (float)accSteps;
               this.outSpikeAcc[o] = 0;
               if (this.outBase[o] < 0.0F) {
                  this.outBase[o] = r;
               }

               boolean active = r > this.outBase[o] * 1.8F + 0.02F * Math.min(1.0F, this.d.outIdx[o].length / 20.0F);
               if (!active) {
                  this.outBase[o] = this.outBase[o] + (r - this.outBase[o]) * 0.02F;
               }

               this.outActive[o] = active;
               rates[o] = r;
               bases[o] = this.outBase[o];
            }

            l.outRate = rates;
            l.outBase = bases;
            if (this.epgAcc > 0L) {
               l.compassAngle = (float)Math.atan2(this.epgSinAcc, this.epgCosAcc);
               l.compassAmp = (float)(Math.hypot(this.epgCosAcc, this.epgSinAcc) / this.epgAcc);
               l.epgRate = (float)this.epgAcc / (float)accSteps;
            }

            this.epgCosAcc = this.epgSinAcc = 0.0;
            this.epgAcc = 0L;
            if (this.plastScale != null) {
               double sa = 0.0;
               double sp = 0.0;
               int na = 0;
               int np = 0;
               byte[] mc = this.d.mbonClass;

               for (int p = 0; p < this.plastScale.length; p++) {
                  byte c = mc[this.d.plastTarget[p]];
                  if (c == 1) {
                     sa += this.plastScale[p];
                     na++;
                  } else if (c == 2) {
                     sp += this.plastScale[p];
                     np++;
                  }
               }

               l.memAvoid = na > 0 ? (float)(sa / na) : 1.0F;
               l.memApproach = np > 0 ? (float)(sp / np) : 1.0F;
            }

            this.loco = l;
            spkAcc = 0L;
            accSteps = 0L;
            dnR = 0L;
            dnL = 0L;
            lastPost = now;
         }

         if (steps == 0) {
            try {
               Thread.sleep(1L);
            } catch (InterruptedException var37) {
               break;
            }
         }
      }

      this.running = false;
      RUNNING.decrementAndGet();
   }

   private void plasticity(int pamNow, int ppl1Now) {
      this.pamLP = this.pamLP + (pamNow - this.pamLP) * 0.01F;
      this.ppl1LP = this.ppl1LP + (ppl1Now - this.ppl1LP) * 0.01F;
      float[] tr = this.kcTrace;
      if (++this.plastTick >= 20) {
         this.plastTick = 0;
         if (this.pamBase < 0.0F) {
            this.pamBase = this.pamLP;
            this.ppl1Base = this.ppl1LP;
         }

         float pamGate = Math.max(0.0F, this.pamLP - Math.max(this.pamBase * 1.8F, 0.4F));
         float ppl1Gate = Math.max(0.0F, this.ppl1LP - Math.max(this.ppl1Base * 1.8F, 0.04F));
         if (pamGate <= 0.0F) {
            this.pamBase = this.pamBase + (this.pamLP - this.pamBase) * 0.01F;
         }

         if (ppl1Gate <= 0.0F) {
            this.ppl1Base = this.ppl1Base + (this.ppl1LP - this.ppl1Base) * 0.01F;
         }

         pamGate = Math.min(1.0F, pamGate / 2.0F);
         ppl1Gate = Math.min(1.0F, ppl1Gate / 0.12F);
         float decay20 = (float)Math.pow(TRACE_DECAY, 20.0);
         int[] src = this.d.plastSource;
         int[] tgt = this.d.plastTarget;
         byte[] mc = this.d.mbonClass;
         float[] sc = this.plastScale;
         boolean gated = pamGate > 0.0F || ppl1Gate > 0.0F;
         float elig = 0.5F;
         if (gated) {
            if (this.kcList == null) {
               this.kcList = Arrays.stream(src).distinct().toArray();
            }

            double m = 0.0;
            double m2 = 0.0;

            for (int i : this.kcList) {
               m += tr[i];
               m2 += tr[i] * tr[i];
            }

            m /= this.kcList.length;
            double sd = Math.sqrt(Math.max(0.0, m2 / this.kcList.length - m * m));
            elig = (float)Math.max(0.5, m + 1.5 * sd);
         }

         for (int p = 0; p < sc.length; p++) {
            float v = sc[p];
            if (gated) {
               float t = tr[src[p]];
               if (t > elig) {
                  byte c = mc[tgt[p]];
                  if (c == 1 && pamGate > 0.0F) {
                     v *= 1.0F - 0.0072F * this.gEta * t * pamGate;
                  } else if (c == 2 && ppl1Gate > 0.0F) {
                     v *= 1.0F - 0.24F * this.gEta * t * ppl1Gate;
                  }
               }
            }

            v += (1.0F - v) * this.gForget;
            sc[p] = Math.max(0.05F, v);
         }

         for (int i = 0; i < tr.length; i++) {
            if (tr[i] > 0.0F) {
               tr[i] = tr[i] < 0.01F ? 0.0F : tr[i] * decay20;
            }
         }
      }
   }

   public float[] memory() {
      WholeBrain.Loco l = this.loco;
      return l == null ? new float[]{1.0F, 1.0F} : new float[]{l.memAvoid, l.memApproach};
   }

   public float[] dopamine() {
      return new float[]{this.pamLP, this.pamBase, this.ppl1LP, this.ppl1Base};
   }

   private int step(float[] b, long[] dn) {
      int n = this.d.n;
      float[] V = this.v;
      float[] I = this.isyn;
      float[] A = this.a;

      for (int i = 0; i < n; i++) {
         float is = I[i] * this.gDecay;
         I[i] = is;
         float ai = A[i] * this.gADecay;
         A[i] = ai;
         V[i] += this.gK * (is - ai - V[i]);
      }

      int[] sens = this.d.sensIdx;
      float nk = sens.length * this.gDriveRate;
      int nki = (int)nk + (this.rng.nextFloat() < nk - (int)nk ? 1 : 0);

      for (int j = 0; j < nki; j++) {
         I[sens[this.rng.nextInt(sens.length)]] += this.gDriveAmp;
      }

      int[] dns = this.d.dnIdx;
      float nd = dns.length * this.gDnDriveRate;
      int ndi = (int)nd + (this.rng.nextFloat() < nd - (int)nd ? 1 : 0);

      for (int j = 0; j < ndi; j++) {
         I[dns[this.rng.nextInt(dns.length)]] += this.gDriveAmp;
      }

      float cstr = this.compassStrength;
      float chd = this.compassHeading;
      if (cstr > 0.0F && !Float.isNaN(chd) && this.d.epgIdx.length > 0) {
         int[] eidx = this.d.epgIdx;
         if (this.epgDrive == null || this.epgDrive.length != eidx.length) {
            this.epgDrive = new float[eidx.length];
         }

         for (int k = 0; k < eidx.length; k++) {
            float c = (float)Math.cos(this.d.epgAngle[eidx[k]] - chd);
            float tune = c > 0.3F ? (c - 0.3F) / 0.7F : 0.0F;
            float nb = 0.15F * cstr * tune;
            if (nb > 0.0F && this.rng.nextFloat() < nb) {
               I[eidx[k]] = I[eidx[k]] + this.gDriveAmp;
            }
         }
      }

      for (int c = 0; c < b.length && c < this.d.chanIdx.length; c++) {
         float val = b[c];
         if (!(val <= 0.0F)) {
            int[] idx = this.d.chanIdx[c];
            float nb = idx.length * this.gBusRate * val * this.chanGain[c];
            int nbi = (int)nb + (this.rng.nextFloat() < nb - (int)nb ? 1 : 0);

            for (int j = 0; j < nbi; j++) {
               I[idx[this.rng.nextInt(idx.length)]] += this.gDriveAmp;
            }
         }
      }

      for (int r = 0; r < RDEPTH; r++) {
         int[] buf = this.refBuf[r];
         int j = 0;

         for (int m = this.refN[r]; j < m; j++) {
            V[buf[j]] = 0.0F;
         }
      }

      int sn = 0;
      int[] indptr = this.d.indptr;
      int[] indices = this.d.indices;
      float[] w = this.d.weight;
      byte[] sensory = this.d.sensory;
      byte[] dnclass = this.d.dnclass;
      byte[] nchan = this.d.neuronChan;
      long[] omask = this.d.neuronOutMask;
      boolean[] isKC = this.d.isKC;
      int[] ppos = this.d.plastPos;
      float[] pscale = this.plastScale;
      float[] eang = this.d.epgAngle;
      boolean compass = eang != null && eang.length == n;
      int pamNow = 0;
      int ppl1Now = 0;
      float vth = this.gVth;
      boolean ga = this.groupsActive;
      byte[] eg = this.d.edgeGroups;
      float[] mm = this.maskMul;

      for (int i = 0; i < n; i++) {
         if (V[i] >= vth) {
            V[i] = 0.0F;
            this.spk[sn++] = i;
            int s = indptr[i];
            int e = indptr[i + 1];
            float g = sensory[i] != 0 ? this.gSyn * this.gSens : this.gSyn;
            float gi = g * this.gInh;
            if (pscale != null && isKC[i]) {
               this.kcTrace[i] = Math.min(1.0F, this.kcTrace[i] + 0.34F);

               for (int q = s; q < e; q++) {
                  float ww = w[q];
                  int pp = ppos[q];
                  if (pp >= 0) {
                     ww *= pscale[pp];
                  }

                  if (ga && eg[q] != 0) {
                     ww *= mm[eg[q] & 255];
                  }

                  I[indices[q]] = I[indices[q]] + (ww > 0.0F ? ww * g : ww * gi);
               }
            } else {
               for (int q = s; q < e; q++) {
                  float ww = w[q];
                  if (ga && eg[q] != 0) {
                     ww *= mm[eg[q] & 255];
                  }

                  I[indices[q]] = I[indices[q]] + (ww > 0.0F ? ww * g : ww * gi);
               }
            }

            if (compass && !Float.isNaN(eang[i])) {
               this.epgCosAcc = this.epgCosAcc + Math.cos(eang[i]);
               this.epgSinAcc = this.epgSinAcc + Math.sin(eang[i]);
               this.epgAcc++;
            }

            if (sensory[i] == 0) {
               A[i] += this.gAdaptInc;
            }

            byte dc = dnclass[i];
            if (dc == 1) {
               dn[0]++;
            } else if (dc == 2) {
               dn[1]++;
            }

            byte ch = nchan[i];
            if (ch >= 0) {
               this.chanSpikeAcc[ch]++;
            }

            for (long om = omask == null ? 0L : omask[i]; om != 0L; om &= om - 1L) {
               int ob = Long.numberOfTrailingZeros(om);
               this.outSpikeAcc[ob]++;
               if (ob == this.pamOut) {
                  pamNow++;
               } else if (ob == this.ppl1Out) {
                  ppl1Now++;
               }
            }
         }
      }

      if (pscale != null) {
         this.plasticity(pamNow, ppl1Now);
      }

      int[] cur = this.refBuf[this.refCur];
      System.arraycopy(this.spk, 0, cur, 0, sn);
      this.refN[this.refCur] = sn;
      this.refCur = (this.refCur + 1) % RDEPTH;
      return sn;
   }

   public static final class Data {
      public final int n;
      public final int nEdges;
      public final int[] indptr;
      public final int[] indices;
      public final float[] weight;
      public final byte[] sensory;
      public final byte[] dnclass;
      public final int[] sensIdx;
      public final int[] dnIdx;
      public final String[] chanNames;
      public final int[][] chanIdx;
      public final byte[] neuronChan;
      public String[] outNames = new String[0];
      public String[] outLabels = new String[0];
      public int[][] outIdx = new int[0][];
      public long[] neuronOutMask;
      public int[] epgIdx = new int[0];
      public float[] epgAngle = new float[0];
      public int[] plastPos;
      public int nPlastic;
      public byte[] mbonClass;
      public boolean[] isKC;
      public int[] plastTarget;
      public int[] plastSource;
      public float[] edgeGain;
      public byte[] edgeGroups;
      public String[] groupNames = new String[0];
      public String circuitGainNote = "";

      Data(int n, int nEdges, int[] indptr, int[] indices, float[] weight, byte[] sensory, byte[] dnclass, String[] chanNames, int[][] chanIdx) {
         this.n = n;
         this.nEdges = nEdges;
         this.indptr = indptr;
         this.indices = indices;
         this.weight = weight;
         this.sensory = sensory;
         this.dnclass = dnclass;
         this.chanNames = chanNames;
         this.chanIdx = chanIdx;
         List<Integer> s = new ArrayList<>();
         List<Integer> d = new ArrayList<>();

         for (int i = 0; i < n; i++) {
            if (sensory[i] != 0) {
               s.add(i);
            }

            if (dnclass[i] != 0) {
               d.add(i);
            }
         }

         this.sensIdx = s.stream().mapToInt(Integer::intValue).toArray();
         this.dnIdx = d.stream().mapToInt(Integer::intValue).toArray();
         this.neuronChan = new byte[n];
         Arrays.fill(this.neuronChan, (byte)-1);

         for (int c = 0; c < chanIdx.length; c++) {
            for (int i : chanIdx[c]) {
               if (i >= 0 && i < n && this.neuronChan[i] < 0) {
                  this.neuronChan[i] = (byte)c;
               }
            }
         }
      }

      public int out(String name) {
         for (int i = 0; i < this.outNames.length; i++) {
            if (this.outNames[i].equals(name)) {
               return i;
            }
         }

         return -1;
      }

      public int channel(String name) {
         for (int i = 0; i < this.chanNames.length; i++) {
            if (this.chanNames[i].equals(name)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static final class Loco {
      public final float turnBias;
      public final float walkDrive;
      public final float activity;
      public final float[] chanSpikes;
      public final float stepsPerSec;
      public final float dnLeft;
      public final float dnRight;
      public float[] outRate = new float[0];
      public float[] outBase = new float[0];
      public float compassAngle = Float.NaN;
      public float compassAmp;
      public float epgRate;
      public float memAvoid = 1.0F;
      public float memApproach = 1.0F;

      Loco(float turnBias, float walkDrive, float activity, float[] chanSpikes, float stepsPerSec, float dnLeft, float dnRight) {
         this.turnBias = turnBias;
         this.walkDrive = walkDrive;
         this.activity = activity;
         this.chanSpikes = chanSpikes;
         this.stepsPerSec = stepsPerSec;
         this.dnLeft = dnLeft;
         this.dnRight = dnRight;
      }
   }
}

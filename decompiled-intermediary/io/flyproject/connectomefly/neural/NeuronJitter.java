package io.flyproject.connectomefly.neural;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public final class NeuronJitter {
   private NeuronJitter() {
   }

   public static double of(String neuronId) {
      CRC32 crc = new CRC32();
      crc.update(neuronId.getBytes(StandardCharsets.UTF_8));
      long h = crc.getValue();
      return 0.9 + 0.2 * (h % 1000L) / 1000.0;
   }
}

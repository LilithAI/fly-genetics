package io.flyproject.connectomefly.lab;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2561;
import net.minecraft.class_2561.class_2562;

public final class BoxBooks {
   private static final Map<String, String[]> PAGES = new LinkedHashMap<>();

   private BoxBooks() {
   }

   private static void put(String id, String... pages) {
      PAGES.put(id, pages);
   }

   public static class_1799 book(String id, String title) {
      class_1799 stack = new class_1799(class_1802.field_8360);
      class_2487 nbt = stack.method_7948();
      String t = title.replaceAll("^[0-9]+ ", "");
      nbt.method_10582("title", t.length() > 30 ? t.substring(0, 30) : t);
      nbt.method_10582("author", "the fly project");
      class_2499 pages = new class_2499();

      for (String p : PAGES.getOrDefault(id, PAGES.get("home"))) {
         pages.add(class_2519.method_23256(class_2562.method_10867(class_2561.method_43470(p))));
      }

      nbt.method_10566("pages", pages);
      return stack;
   }

   static {
      put(
         "escape",
         "ESCAPE\n\nA fly that sees something growing fast in its view jumps before it knows what it is. This box shows that reflex.",
         "WHAT TO DO\n\nPress the stone button inside the door. A black shape appears near her and grows for about 2 seconds.\n\nOr sprint straight at her.",
         "WHAT SHE DOES\n\nShe jumps and flies off, away from the shape.\n\nIf the shape is directly behind her she does nothing. A fly cannot see the last sixth of the circle behind it.",
         "NEURONS\n\nLC4, LPLC1, LPLC2 (looming detectors in the eye)\n-> the Giant Fiber\n-> TTMn, the motor neuron of the jump muscle.\n\nReal wiring. The HUD shows TTMn FIRED."
      );
      put(
         "looming",
         "LOOMING\n\nThe same reflex as the Escape box, made into a test of where she can see.",
         "WHAT TO DO\n\nPress the stone button. The shape appears on a random side each time. The black floor tiles mark the eight directions.\n\n/flylab loom 180 puts it right behind her.",
         "WHAT SHE DOES\n\nFront or side: she jumps.\nBehind: nothing at all.\n\nA body sprinting at her from behind still scares her. That is the air it pushes, not sight. The shape has no air.",
         "NEURONS\n\nLC4 / LPLC2 -> Giant Fiber -> TTMn.\n\nThe size of her blind cone behind is our number (about 60 degrees). The circuit is real."
      );
      put(
         "steering",
         "STEERING\n\nOnce she is running from something, which way does she turn? Two pairs of descending neurons decide.",
         "WHAT TO DO\n\nSprint at her so she has to run between the pillars.",
         "WHAT SHE DOES\n\nShe turns away from the side the threat is on and runs, curving round the pillars.",
         "NEURONS\n\nDNa01 and DNa02 (left and right steering neurons)\n-> 313 leg motor neurons.\n\nThe side that fires more is the side she turns to."
      );
      put(
         "touch",
         "TOUCH\n\nA fly's body is covered in bristles. Each one has a neuron. Bend one and the legs answer before the brain is asked.",
         "WHAT TO DO\n\nPunch her once, gently. She has 10 health.",
         "WHAT SHE DOES\n\nA short scramble or a hop. Then she stops and cleans herself: something touched her, so she grooms.",
         "NEURONS\n\n644 bristle neurons\n-> leg motor neurons.\n\nA reflex arc. The HUD shows 'tactile' and the leg motor neurons bursting."
      );
      put(
         "hearing",
         "HEARING\n\nA fly hears with its antennae. Sound shakes them, and a ring of neurons at the base (Johnston's organ) feels the shake.",
         "WHAT TO DO\n\nClick the note block, ring the bell, or take a music disc from the chest and put it in the jukebox.",
         "WHAT SHE DOES\n\nA loud sound startles her and she takes off. The hearing neurons connect straight onto the jump neuron.",
         "NEURONS\n\nJohnston's organ, JO-A and JO-B cells\n-> 1221 synapses onto the Giant Fiber.\n\nThe HUD shows 'auditory'."
      );
      put(
         "navigation",
         "NAVIGATION\n\nA fly that walks away from a spot can find its way back without seeing it, by keeping count of its own steps and turns.",
         "WHAT TO DO\n\nJust watch. Home is the leaf (the flowering azalea leaves block in the corner). She rests on it, walks off on an outing, then comes back. The cake is only there to smell.",
         "WHAT SHE DOES\n\nOut: a wander. Back: a straight line to where her count says home is. She often misses by a block or two, and so do real flies. For the last steps she sees the leaf.",
         "NEURONS\n\n45 EPG cells form a ring where one bump of activity points like a compass needle. They are her own cells, read live.\n\nAdding up the steps on that compass is our model (labelled)."
      );
      put(
         "memory",
         "MEMORY\n\nA fly can learn that a smell means trouble. The lesson is stored in the mushroom body, as weaker synapses.",
         "THE TWO FOODS\n\nCake (left) and melon (right) smell different to her. They excite different receptor neurons, so different Kenyon cells carry each smell.",
         "WHAT TO DO\n\n1. She flies to one food and eats.\n2. Punch her 2 or 3 times WHILE she eats it.\n3. Type /flylab hungry and watch which food she picks now.",
         "WHAT SHE DOES\n\nShe avoids the food she was hit at and goes to the other one. The HUD Memory line shows the verdict for that smell going negative. It fades over about 15 minutes.",
         "NEURONS\n\nPain makes the PPL1 dopamine neurons fire. The Kenyon cells active at that moment (that smell) have their synapses onto the 'approach' output neurons (MBONs) weakened.",
         "WHAT IS REAL\n\nThe receptor neurons, Kenyon cells, dopamine cells, MBONs and every synapse are the real map. The learning rule is the published one. Which smell receptors a cake excites is our pick."
      );
      put(
         "aggression",
         "AGGRESSION\n\nFemale flies fight over food. They lunge and shove. A small group of neurons switches this on.",
         "WHAT TO DO\n\nJust watch. Two hungry females and one cake.",
         "WHAT SHE DOES\n\nThey face each other, one lunges and shoves. The one that was shoved backs off for a few seconds and the winner eats. Then it may start again.",
         "NEURONS\n\npC1d / pC1e (the aggression switch)\n-> DNa11 and DNp37 descending neurons.\n\nShe lunges only when those fire. The loser backing off is our model (labelled)."
      );
      put(
         "courtship",
         "COURTSHIP\n\nThe male sings by vibrating one wing. The female decides. Her answer builds up slowly in a few neurons.",
         "WHAT TO DO\n\nJust watch. /flylab mate starts it over.",
         "WHAT SHE DOES\n\nHe follows her and sings (you can hear it). She slows down. After about 10 seconds of song she accepts, they mate for 10 seconds, and she lays an egg that hatches in 30.",
         "NEURONS\n\nHis song -> her JO-B hearing cells -> her pC1 cells. When pC1 has fired enough, that is her yes (HUD: Courtship).\n\nThe link from hearing to pC1 is a bridge we added (labelled)."
      );
      put(
         "taste",
         "TASTE\n\nA fly tastes with its feet and with the tip of its proboscis. Sweet, bitter and water each have their own neurons.",
         "WHAT TO DO\n\nWatch her walk onto things. /flylab hungry and /flylab thirsty speed it up.\n\nCake, honey, melon = sugar.\nWither rose = bitter.\nThe cauldron = water.",
         "WATER\n\nA thirsty fly goes to the cauldron and drinks (you hear drinking, not eating). You can also right click her with a bottle of water and she drinks from it.",
         "WHAT SHE DOES\n\nSugar under her feet: the proboscis comes out and she eats. Thirsty at water: she drinks. Bitter: the proboscis stays in.",
         "NEURONS\n\nSugar: Gr5a / Gr64f.\nBitter: Gr33a / Gr66a.\nWater: ppk28.\nThey sit on the legs and on the labellum. The HUD Taste line says which are firing."
      );
      put(
         "smell",
         "SMELL\n\nShe finds food by smell from across the box, with receptor neurons on her antennae.",
         "WHAT TO DO\n\nShe starts hungry. Watch her find the cake at the far end. The campfire in the corner is smoke (CO2), which she dislikes.",
         "WHAT SHE DOES\n\nShe flies over, lands, walks the last steps and eats.",
         "NEURONS\n\n1655 olfactory receptor neurons (903 for the smell of cake, 752 for fruit smells).\n\nThe HUD shows olf_food firing harder as she gets closer."
      );
      put(
         "hunger",
         "HUNGER\n\nThe same cake smells stronger to a hungry fly. Hunger turns up the volume on food.",
         "WHAT TO DO\n\nShe starts hungry and eats. When she is full she ignores the food. /flylab hungry makes her hungry again.",
         "WHAT SHE DOES\n\nHungry: straight to the food. Full: she walks past it. After a meal her insulin cells fire for a minute and she is not interested.",
         "NEURONS\n\nIPCs (insulin producing cells) fire after a meal. They are real cells (HUD: Satiety).\n\nHunger raising the gain on smell is our model (labelled)."
      );
      put(
         "lure",
         "LURE\n\nYou can lead her with a smell.",
         "WHAT TO DO\n\nTake a flower or berries from the chest, hold it, and walk along the glass or inside the box. There is also a lead in the chest if you want to walk her somewhere.",
         "WHAT SHE DOES\n\nShe follows the smell in your hand. A hungry fly follows much more eagerly (/flylab hungry).",
         "NEURONS\n\n752 fruit smell receptor neurons, the same ones a real fruit drives."
      );
      put(
         "heat",
         "HEAT\n\nFlies sense heat from a distance and turn away long before it burns.",
         "WHAT TO DO\n\nBring her near the lava. Hold a flower so she follows you, or put her on a lead (a lead only moves her body, her brain stays her own). Or just wait until she wanders near it.",
         "WHAT SHE DOES\n\nShe recoils and flies off. If she is actually burned it also counts as a punishment (see the Memory box).",
         "NEURONS\n\nGr28a heat neurons in the antenna, and 180 pain neurons in the body wall."
      );
      put(
         "water",
         "WATER\n\nOpen water is a death trap for a fly: wet wings cannot fly.",
         "WHAT TO DO\n\nBring her toward the pool with a flower in your hand or on a lead. Or wait.",
         "WHAT SHE DOES\n\nShe steers away from it. If she ends up in it she struggles out. A thirsty fly drinks from a cauldron or damp ground instead (see the Taste and Damp boxes).",
         "NEURONS\n\n20 humid air and 35 dry air sensing neurons in the antenna."
      );
      put(
         "sleep",
         "SLEEP\n\nFlies sleep at night. Two groups of neurons fight it out: one says 'it is light, stay up', the other counts how long she has been awake.",
         "WHAT TO DO\n\nType /time set 14000 (night) and wait about two minutes. /time set 1000 wakes her.",
         "WHAT SHE DOES\n\nAt night she stops, her body sinks down onto the floor, her head droops and her legs fold in. She stays like that until morning. At dawn and dusk she is most active.",
         "NEURONS\n\nR5 (sleep pressure) against l-LNv (light). s-LNv and LNd are the morning and evening clock cells. See the HUD Sleep and Clock lines."
      );
      put(
         "weather",
         "RAIN\n\nA raindrop weighs more than a fly.",
         "WHAT TO DO\n\nType /weather rain. This box has no glass roof so the rain gets in.",
         "WHAT SHE DOES\n\nShe runs or flies under the wooden roof and waits there until it stops.",
         "NEURONS\n\nIr68a / Ir40a moisture sensing neurons fire when she is wet."
      );
      put(
         "cold",
         "COLD\n\nFlies avoid cold ground just as they avoid heat.",
         "WHAT TO DO\n\nWait until she wanders to the snow, or walk her there on a lead.",
         "WHAT SHE DOES\n\nShe backs off the snow and stays on the warm side.",
         "NEURONS\n\n41 cooling neurons (Ir21a) in the antenna."
      );
      put(
         "wind",
         "WIND\n\nFlies feel wind with their antennae and turn to face it. That is how they track a smell to its source.",
         "WHAT TO DO\n\nThe block with the iron bars is a fan blowing across the box. The white puffs are the wind. Just watch.",
         "WHAT SHE DOES\n\nIn the stream she turns to face the fan and walks toward it against the push, then stops near it.",
         "NEURONS\n\n792 wind sensing neurons in Johnston's organ, the same organ she hears with."
      );
      put(
         "light",
         "LIGHT\n\nFlies walk toward light.",
         "WHAT TO DO\n\nThe box is dark glass. Watch her find the glowstone.",
         "WHAT SHE DOES\n\nShe goes to the bright end and stays near it.",
         "NEURONS\n\n7548 neurons of the visual system carry the light signal."
      );
      put(
         "mould",
         "MOULD\n\nOne smell means 'this food will kill you': geosmin, the smell of mould. Flies have one receptor just for it.",
         "WHAT TO DO\n\nShe starts hungry. The cake sits in the mouldy corner (mycelium and mushrooms).",
         "WHAT SHE DOES\n\nShe wants the cake but will not go into the corner. This one is built in. She never had to learn it.",
         "NEURONS\n\nOr56a receptor neurons: a private line for geosmin."
      );
      put(
         "grooming",
         "GROOMING\n\nFlies clean themselves constantly, always in the same order: eyes and head first, then wings, then hind legs.",
         "WHAT TO DO\n\nLeave her alone and watch her rest.",
         "WHAT SHE DOES\n\nBetween walks she stops and grooms: front legs over the head, hind legs over the wings, legs rubbed together.",
         "NEURONS\n\n6054 bristle neurons report dust and antennal motor neurons start a bout. The order of the steps is our model (labelled)."
      );
      put(
         "terrain",
         "TERRAIN\n\nShe knows where her own legs are.",
         "WHAT TO DO\n\nWatch her walk, or walk her to the steps on a lead. The low slabs are steps. The brick wall is too high to step onto.",
         "WHAT SHE DOES\n\nShe walks up the steps. At the wall she turns along it, flies over, or climbs it (see Surfaces).",
         "NEURONS\n\n2903 proprioceptive neurons in the legs (chordotonal organs)."
      );
      put(
         "bitter",
         "BITTER\n\nBitter means poison. It is a taste, not a smell: she has to touch it to know.",
         "WHY A CAKE AND ROSES\n\nMinecraft has no block of food that poisons a mob. So we took a cake and put wither roses against it: that cake counts as laced with something bitter.",
         "WHAT TO DO\n\nShe starts hungry. Just watch her go to the cake.",
         "WHAT SHE DOES\n\nShe goes to the cake, tastes it, keeps her proboscis in, and after a moment walks away and leaves it alone.",
         "NEURONS\n\nGr33a / Gr66a bitter neurons on the legs and labellum. They shut down feeding directly (real)."
      );
      put(
         "co2",
         "CO2\n\nSmoke and breath mean danger. Flies avoid CO2 without learning to.",
         "WHAT TO DO\n\nShe starts hungry. The campfire is between her and the cake.",
         "WHAT SHE DOES\n\nShe goes round the smoke, not through it, and eats. Once she is full she keeps away from the smoke.",
         "NEURONS\n\nGr21a / Gr63a CO2 receptor neurons."
      );
      put(
         "pain",
         "PAIN\n\nWhat happens when food hurts?",
         "WHAT TO DO\n\nShe starts hungry. There are cactuses in front of the cake.",
         "WHAT SHE DOES\n\nShe goes for the cake, gets pricked, recoils and flies off. Because it hurt while she smelled cake, she now avoids the cake smell (see Memory).",
         "NEURONS\n\n180 pain neurons drive the leg reflex, and also the PPL1 dopamine cells (the lesson)."
      );
      put(
         "damp",
         "DAMP\n\nA thirsty fly looks for water. She prefers open water she can stand next to (a cauldron), but wet mud will do if that is all there is.",
         "WHAT TO DO\n\nShe starts thirsty. The mud patch is in the far corner. /flylab thirsty repeats it. You can also right click her with a bottle of water.",
         "WHAT SHE DOES\n\nShe walks to the mud, puts her proboscis down and drinks (you hear drinking). The HUD Thirst number drops.",
         "NEURONS\n\nIr68a / Ir40a humidity neurons lead her there. ppk28 water taste neurons start the drinking."
      );
      put(
         "dry",
         "DRY\n\nThe opposite sensors: dry air and dry ground.",
         "WHAT TO DO\n\nShe starts on the sand half with her dry sensors already firing. Just watch.",
         "WHAT SHE DOES\n\nShe walks off the sand onto the other half and stays there. In this box only, dry ground pushes her away, so you can see the sensors at work. Everywhere else dry ground just makes her thirsty faster.",
         "NEURONS\n\nIr93a / Ir25a dry air neurons in the antenna."
      );
      put(
         "vision",
         "VISION\n\nShe notices small moving things: another fly, or you at a distance.",
         "WHAT TO DO\n\nWalk past the glass, a few blocks away. Keep your eyes on the HUD, on the line called Vision. That line is where you see what she sees.",
         "WHAT SHE DOES\n\nOften nothing you can see on her body. Noticing is not acting. The Vision line on the HUD says which eye saw you, and her descending neurons decide whether it matters.",
         "NEURONS\n\nLC11 and LC10a: small object detectors in each eye."
      );
      put(
         "optomotor",
         "OPTOMOTOR\n\nIf the whole world seems to rotate, a fly turns with it. That is how it keeps a straight course.",
         "WHAT TO DO\n\n/flylab drum left\n/flylab drum right\n/flylab drum stop",
         "WHAT SHE DOES\n\nThe striped ring turns and she turns the same way, standing or walking. Real flies do this on foot as well as in flight.",
         "NEURONS\n\nT4 / T5 motion detectors -> HS cells. In this brain map only the LEFT HS cells tell direction apart (the right eye's map is over dense), so we read those."
      );
      put(
         "social",
         "SOCIAL\n\nFlies like company. They gather where other flies are, especially on food.",
         "WHAT TO DO\n\nJust watch three slightly hungry females and a cake.",
         "WHAT SHE DOES\n\nThey drift together and eat side by side, without fighting.",
         "WHY NO FIGHT HERE\n\nTheir aggression neurons need two things at once: real hunger, and a rival close to the food. These three are only a little hungry and there is cake for all, so the switch stays off.",
         "HOWEVER\n\nIf they get very hungry and one stands almost on top of another at the food, the aggression neurons (pC1d / pC1e) do fire and they squabble. The Aggression box is built to show that.",
         "NEURONS\n\nOr47b / Or67d pheromone receptor neurons. The pull toward other flies is our model (labelled)."
      );
      put(
         "surfaces",
         "SURFACES\n\nA fly's feet stick. Walls and ceilings are just more floor.",
         "WHAT TO DO\n\n/flylab fly makes her take off. When she meets a wall or the low roof she lands on it.",
         "WHAT SHE DOES\n\nShe walks up the glass and hangs from the roof, same gait, same brain.",
         "NEURONS\n\nNone special, and that is the point. The same leg motor neurons, whichever way is up."
      );
      put(
         "home",
         "HOME\n\nNo experiment. Every sense is on. This is where she just lives.",
         "WHAT SHE DOES\n\nShe walks, stops, grooms, sleeps at night. When she moves, it is because her own descending neurons fired more than their average.",
         "NEURONS\n\nAll 158,262, stepped 1000 times a second."
      );
   }
}

package io.flyproject.connectomefly.neural;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class Circuit {
   public List<Circuit.Node> nodes;
   public List<Circuit.Edge> edges;

   public static Circuit load(Reader reader) {
      return (Circuit)new Gson().fromJson(reader, Circuit.class);
   }

   public static Circuit loadResource(String path) {
      InputStream in = Circuit.class.getResourceAsStream(path);
      if (in == null) {
         throw new IllegalStateException("bundled circuit resource missing: " + path);
      } else {
         return load(new InputStreamReader(in, StandardCharsets.UTF_8));
      }
   }

   public static final class Edge {
      public String pre;
      public String post;
      public double weight;
   }

   public static final class Node {
      public String id;
      public String cell_type;
      public String role;
      public String side;
      public int sign;
   }
}

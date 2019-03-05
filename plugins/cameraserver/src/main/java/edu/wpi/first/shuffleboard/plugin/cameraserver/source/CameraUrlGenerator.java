package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;

import com.google.common.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Generates parameterized URLs for an HTTP camera.
 */
public final class CameraUrlGenerator {

  private final CameraServerSource source;

  /**
   * Creates a new URL generator.
   *
   * @param source the source to generate URLs for
   */
  public CameraUrlGenerator(CameraServerSource source) {
    this.source = Objects.requireNonNull(source, "Null source");
  }

  /**
   * Generates parameterized URLs for the camera source.
   *
   * @param baseUrls the base stream URLs
   */
  public String[] generateUrls(String[] baseUrls) { // NOPMD varargs instead of array
    Map<String, String> commands = new LinkedHashMap<>();
    Resolution resolution = source.getTargetResolution();
    if (resolution != null && resolution.getWidth() > 0 && resolution.getHeight() > 0) {
      commands.put("resolution", resolution.getWidth() + "x" + resolution.getHeight());
    }
    int compression = source.getTargetCompression();
    if (compression >= 0 && compression <= 100) {
      commands.put("compression", Integer.toString(compression));
    }
    int frameRate = source.getTargetFps();
    if (frameRate > 0) {
      commands.put("fps", Integer.toString(frameRate));
    }
    // Add a special case command for NI Cameras
    if (source.getName().contains("IMAQdx")) {
      commands.put("name", source.getName());
    }
    return generateUrls(commands, baseUrls, source.getName());
  }

  @VisibleForTesting
  static String[] generateUrls(Map<String, String> commands, String[] baseUrls,
                               String cameraName) {
    if (baseUrls == null || baseUrls.length == 0) {
      return new String[0];
    }
    if (commands.isEmpty()) {
      return baseUrls;
    } else {
      var urls =  Arrays.stream(baseUrls)
          .map(url -> addHttpParams(url, commands))
          .filter(url -> url != null)
          .toArray(String[]::new);
      return urls;
    }
  }

  @VisibleForTesting
  static String addHttpParams(String input, Map<String, String> commands) {
    if (commands.isEmpty()) {
      return input;
    }
    // Parse the URI
    URI uri;
    try {
      uri = new URI(input);
    } catch (URISyntaxException ex) {
      return input;
    }

    String query = uri.getQuery();

    if (query != null) {
      // Handle the NI special case
      String niName = commands.getOrDefault("name", null);

      String[] existingCommands = query.split("&");
      for (String command : existingCommands) {
        String[] commandSplit = command.split("=");
        if (commandSplit.length != 2) {
          continue;
        }
        try {
          commands.put(URLDecoder.decode(commandSplit[0], "utf-8"), URLDecoder.decode(commandSplit[1], "utf-8"));
        } catch (UnsupportedEncodingException e1) {
          commands.put(commandSplit[0], commandSplit[1]);
        }
      }
      if (niName != null) {
        commands.put("name", niName);
      }
    }

    var queryStr = commands.entrySet().stream()
        .map(e -> httpUrlEncode(e))
        .collect(Collectors.joining("&"));
      return encodeUri(uri, queryStr);

  }

  private static String encodeUri(URI uri, String queryStr) {
    StringBuilder builder = new StringBuilder();
    builder.append(uri.getScheme());
    builder.append("://");
    builder.append(uri.getAuthority());
    builder.append(uri.getPath());
    builder.append('?');
    builder.append(queryStr);
    String fragment = uri.getFragment();
    if (fragment != null) {
      builder.append('#');
      builder.append(fragment);
    }
    return builder.toString();
  }

  private static String httpUrlEncode(Map.Entry<String, String> rawCommand) {
    try {
      return URLEncoder.encode(rawCommand.getKey(), "utf-8").replaceAll("\\+", "%20") + "="
          + URLEncoder.encode(rawCommand.getValue(), "utf-8").replaceAll("\\+", "%20") ;
    } catch (UnsupportedEncodingException ex) {
      return rawCommand.getKey() + "=" + rawCommand.getValue();
    }
  }
}

package edu.wpi.first.shuffleboard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import edu.wpi.first.shuffleboard.sources.Sources;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;
import javafx.beans.property.Property;

import java.util.logging.Level;
import java.util.logging.Logger;

@AnnotatedTypeAdapter(forType = Widget.class)
public class WidgetSaver implements ElementTypeAdapter<Widget> {
  private static final Logger log = Logger.getLogger(WidgetSaver.class.getName());

  @Override
  public JsonElement serialize(Widget src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());
    object.addProperty("_source", src.getSource().getId());
    for (Property p : src.getProperties()) {
      object.add(p.getName(), context.serialize(p.getValue()));
    }
    return object;
  }

  @Override
  public Widget deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    String type = obj.get("_type").getAsString();
    Widget widget = Widgets.typeFor(type)
            .orElseThrow(() -> new JsonParseException("No widget found for " + type)).get();

    String source = obj.get("_source").getAsString();
    try {
      widget.setSource(Sources.forUri(source));
    } catch (RuntimeException e) {
      log.log(Level.WARNING, "Skipping source " + source, e);
    }

    for (Property p : widget.getProperties()) {
      p.setValue(context.deserialize(obj.get(p.getName()), p.getValue().getClass()));
    }

    return widget;
  }
}
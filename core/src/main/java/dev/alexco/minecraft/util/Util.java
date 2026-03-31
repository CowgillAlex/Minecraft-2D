package dev.alexco.minecraft.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import dev.alexco.registry.ResourceLocation;

public class Util {
    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }

    /**
     * Builds a translation-style id from a type prefix and registry id.
     */
     public static String makeDescriptionId(String string, @Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return string + ".unregistered_sadface";
        }
        return string + '.' + resourceLocation.getNamespace() + '.' + resourceLocation.getPath().replace('/', '.');
    }


    public static JsonArray getArray(final JsonObject object, final String element) {
        if (object.has(element)) {
            return asArray(object.get(element), element);
        }
        throw new JsonSyntaxException("Missing " + element + ", expected to find a JsonArray");
    }

    public static JsonArray getArray(final JsonObject object, final String name, @Nullable final JsonArray defaultArray) {
        if (object.has(name)) {
            return asArray(object.get(name), name);
        }
        return defaultArray;
    }
     public static JsonArray asArray(final JsonElement element, final String name) {
        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a JsonArray, was " + getType(element));
    }

    /**
     * Returns a concise human-readable type description for JSON parse errors.
     */
     public static String getType(final JsonElement element) {
        final String string = StringUtils.abbreviateMiddle(String.valueOf(element), "...", 10);
        if (element == null) {
            return "null (missing)";
        }
        if (element.isJsonNull()) {
            return "null (json)";
        }
        if (element.isJsonArray()) {
            return "an array (" + string + ")";
        }
        if (element.isJsonObject()) {
            return "an object (" + string + ")";
        }
        if (element.isJsonPrimitive()) {
            final JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
            if (jsonPrimitive.isNumber()) {
                return "a number (" + string + ")";
            }
            if (jsonPrimitive.isBoolean()) {
                return "a boolean (" + string + ")";
            }
        }
        return string;
    }
     public static String asString(final JsonElement element, final String name) {
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a string, was " + getType(element));
    }
     public static boolean asBoolean(final JsonElement element, final String name) {
        if (element.isJsonPrimitive()) {
            return element.getAsBoolean();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a Boolean, was " + getType(element));
    }

    public static boolean getBoolean(final JsonObject object, final String element) {
        if (object.has(element)) {
            return asBoolean(object.get(element), element);
        }
        throw new JsonSyntaxException("Missing " + element + ", expected to find a Boolean");
    }

    public static boolean getBoolean(final JsonObject object, final String element, final boolean defaultBoolean) {
        if (object.has(element)) {
            return asBoolean(object.get(element), element);
        }
        return defaultBoolean;
    }

    public static float asFloat(final JsonElement element, final String name) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsFloat();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a Float, was " + getType(element));
    }

    public static float getFloat(final JsonObject object, final String element) {
        if (object.has(element)) {
            return asFloat(object.get(element), element);
        }
        throw new JsonSyntaxException("Missing " + element + ", expected to find a Float");
    }

    public static float getFloat(final JsonObject object, final String element, final float defaultFloat) {
        if (object.has(element)) {
            return asFloat(object.get(element), element);
        }
        return defaultFloat;
    }

    public static long asLong(final JsonElement element, final String name) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsLong();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a Long, was " + getType(element));
    }

    public static long getLong(final JsonObject object, final String element, final long defaultLong) {
        if (object.has(element)) {
            return asLong(object.get(element), element);
        }
        return defaultLong;
    }

    public static int asInt(final JsonElement element, final String name) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a Int, was " + getType(element));
    }

    public static int getInt(final JsonObject object, final String element) {
        if (object.has(element)) {
            return asInt(object.get(element), element);
        }
        throw new JsonSyntaxException("Missing " + element + ", expected to find a Int");
    }

    public static int getInt(final JsonObject object, final String element, final int defaultInt) {
        if (object.has(element)) {
            return asInt(object.get(element), element);
        }
        return defaultInt;
    }

    public static byte asByte(final JsonElement element, final String name) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsByte();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a Byte, was " + getType(element));
    }

    public static byte getByte(final JsonObject object, final String element, final byte defaultByte) {
        if (object.has(element)) {
            return asByte(object.get(element), element);
        }
        return defaultByte;
    }

    public static JsonObject asObject(final JsonElement element, final String name) {
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a JsonObject, was " + getType(element));
    }

    public static JsonObject getObject(final JsonObject object, final String element) {
        if (object.has(element)) {
            return asObject(object.get(element), element);
        }
        throw new JsonSyntaxException("Missing " + element + ", expected to find a JsonObject");
    }

    public static JsonObject getObject(final JsonObject object, final String element, final JsonObject defaultObject) {
        if (object.has(element)) {
            return asObject(object.get(element), element);
        }
        return defaultObject;
    }



}

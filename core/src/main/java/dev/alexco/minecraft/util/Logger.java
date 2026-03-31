package dev.alexco.minecraft.util;

public class Logger {
    public static void INFO(String text, Object... args){
        System.out.println(String.format("Info: " + text, (Object[]) args));
    }
    public static void INFO(String text){
        System.out.println("Info: "+text);
    }
    public static void ERROR(String text, String... args){
        System.out.println(String.format("Error: " +text, (Object[]) args));
    }
    public static void ERROR(String text, Object... args){
        System.out.println(String.format("Error: " +text,  args));
    }
    public static void ERROR(String text){
        System.out.println("Error: " +text);
    }
    public static void DEBUG(String text, Object... args){
        System.out.println(String.format("Debug: " + text, (Object[]) args));
    }
    public static void DEBUG(String text){
        System.out.println("Debug: "+text);
    }
}

package org.d71.jrulexpr.expression;

public class JrxvReplaceDash {    
    public static void main(String[] args) {
        String input = "jrx-alpha-beta jrxv-bla-die jrxv_ha-ha jrx-test-data normal-string jrx-fixed";
        
        // Regex explanation:
        // (?<=\\bjrx-[\\w-]*)  -> Positive lookbehind: ensure the match is preceded by 'jrx-' 
        //                         at the start of a word (\b), followed by any word chars or dashes.
        // -                    -> The actual character we want to replace.
        
        String regex = "(?<=\\bjrxv?[\\w-]*)-";
        String replacement = "_"; // Replacing dash with underscore

        String result = input.replaceAll(regex, replacement);

        System.out.println("Original: " + input);
        System.out.println("Modified: " + result);
    }
}
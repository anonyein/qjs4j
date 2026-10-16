/*
 * Copyright (c) 2025-2026. caoccao.com Sam Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.caoccao.qjs4j.core;

import java.text.Collator;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Intl.Collator instance object.
 */
public final class JSIntlCollator extends JSObject {
    public static final String NAME = "Intl.Collator";
    private final Collator accentCollator;
    private JSFunction boundCompareFunction;
    private final String caseFirst;
    private final String collation;
    private final Collator collator;
    private final boolean ignorePunctuation;
    private final Locale locale;
    private final boolean numeric;
    private final boolean phonebookMode;
    private final Collator primaryCollator;
    private final String resolvedLocaleTag;
    private final String sensitivity;
    private final String usage;

    public JSIntlCollator(JSContext context, Locale locale, String sensitivity, String usage, String collation,
            boolean numeric, String caseFirst, boolean ignorePunctuation, String resolvedLocaleTag) {
        super(context);
        this.locale = locale;
        this.sensitivity = sensitivity;
        this.usage = usage;
        this.collation = collation;
        this.numeric = numeric;
        this.caseFirst = caseFirst;
        this.ignorePunctuation = ignorePunctuation;
        this.resolvedLocaleTag = resolvedLocaleTag;
        // Phonebook mode: explicit phonebk collation or German search
        this.phonebookMode = "phonebk".equals(collation)
                || ("search".equals(usage) && "de".equals(locale.getLanguage()));
        this.collator = Collator.getInstance(locale);
        switch (sensitivity) {
            case "base" -> collator.setStrength(Collator.PRIMARY);
            case "accent" -> collator.setStrength(Collator.SECONDARY);
            case "case" -> collator.setStrength(Collator.TERTIARY);
            case "variant" -> collator.setStrength(Collator.IDENTICAL);
            default -> collator.setStrength(Collator.TERTIARY);
        }
        // Enable decomposition for canonical equivalence
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        accentCollator = (Collator) collator.clone();
        accentCollator.setStrength(Collator.SECONDARY);
        primaryCollator = (Collator) collator.clone();
        primaryCollator.setStrength(Collator.PRIMARY);
    }

    public int compare(String left, String right) {
        String effectiveLeft = normalizeDigits(left);
        String effectiveRight = normalizeDigits(right);
        if (ignorePunctuation) {
            effectiveLeft = stripPunctuation(effectiveLeft);
            effectiveRight = stripPunctuation(effectiveRight);
        }
        if (phonebookMode) {
            effectiveLeft = expandGermanUmlauts(effectiveLeft);
            effectiveRight = expandGermanUmlauts(effectiveRight);
        }
        if ("case".equals(sensitivity)) {
            effectiveLeft = stripAccents(effectiveLeft);
            effectiveRight = stripAccents(effectiveRight);
        }
        if (numeric) {
            return compareNumeric(effectiveLeft, effectiveRight);
        }
        return compareText(effectiveLeft, effectiveRight);
    }

    private int compareNumeric(String left, String right) {
        int i = 0;
        int j = 0;
        int secondaryResult = 0;
        while (i < left.length() && j < right.length()) {
            context.getVirtualMachine().checkExecutionInterruptForNative();
            if (Character.isDigit(left.codePointAt(i)) && Character.isDigit(right.codePointAt(j))) {
                int leftEnd = digitEnd(left, i);
                int rightEnd = digitEnd(right, j);
                while (i < leftEnd && Character.digit(left.codePointAt(i), 10) == 0) {
                    i += Character.charCount(left.codePointAt(i));
                }
                while (j < rightEnd && Character.digit(right.codePointAt(j), 10) == 0) {
                    j += Character.charCount(right.codePointAt(j));
                }
                int lengthComparison = Integer.compare(left.codePointCount(i, leftEnd),
                        right.codePointCount(j, rightEnd));
                if (lengthComparison != 0) {
                    return lengthComparison;
                }
                while (i < leftEnd) {
                    int a = left.codePointAt(i);
                    int b = right.codePointAt(j);
                    int comparison = Integer.compare(Character.digit(a, 10), Character.digit(b, 10));
                    if (comparison != 0) {
                        return comparison;
                    }
                    i += Character.charCount(a);
                    j += Character.charCount(b);
                }
                i = leftEnd;
                j = rightEnd;
            } else {
                int leftEnd = textEnd(left, i);
                int rightEnd = textEnd(right, j);
                String a = left.substring(i, leftEnd);
                String b = right.substring(j, rightEnd);
                int comparison = compareText(a, b);
                if (comparison != 0) {
                    if (primaryCollator.compare(a, b) != 0) {
                        return comparison;
                    }
                    if (secondaryResult == 0) {
                        secondaryResult = comparison;
                    }
                }
                i = leftEnd;
                j = rightEnd;
            }
        }
        int lengthComparison = Integer.compare(left.length() - i, right.length() - j);
        return lengthComparison != 0 ? lengthComparison : secondaryResult;
    }

    private int compareText(String left, String right) {
        int result = Integer.signum(collator.compare(left, right));
        if (result == 0 || "false".equals(caseFirst) || "base".equals(sensitivity) || "accent".equals(sensitivity)) {
            return result;
        }
        // Case is a tertiary distinction: accents and base letters retain their locale ordering.
        if (accentCollator.compare(left, right) != 0) {
            return result;
        }
        for (int i = 0, j = 0; i < left.length() && j < right.length();) {
            int a = left.codePointAt(i);
            int b = right.codePointAt(j);
            if (Character.isUpperCase(a) != Character.isUpperCase(b)) {
                return Character.isUpperCase(a) == "upper".equals(caseFirst) ? -1 : 1;
            }
            i += Character.charCount(a);
            j += Character.charCount(b);
        }
        return result;
    }

    public JSFunction getBoundCompareFunction() {
        return boundCompareFunction;
    }

    public String getCaseFirst() {
        return caseFirst;
    }

    public String getCollation() {
        return collation;
    }

    public boolean getIgnorePunctuation() {
        return ignorePunctuation;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean getNumeric() {
        return numeric;
    }

    public String getResolvedLocaleTag() {
        return resolvedLocaleTag;
    }

    public String getSensitivity() {
        return sensitivity;
    }

    public String getUsage() {
        return usage;
    }

    public void setBoundCompareFunction(JSFunction boundCompareFunction) {
        this.boundCompareFunction = boundCompareFunction;
    }

    private static int digitEnd(String value, int start) {
        int end = start;
        while (end < value.length() && Character.isDigit(value.codePointAt(end))) {
            end += Character.charCount(value.codePointAt(end));
        }
        return end;
    }

    /**
     * Expand German umlauts for phonebook collation (DIN 5007-2). ä→ae, ö→oe, ü→ue, Ä→Ae, Ö→Oe, Ü→Ue, ß→ss
     */
    private static String expandGermanUmlauts(String input) {
        StringBuilder result = null;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String expansion = switch (c) {
                case '\u00E4' -> "ae"; // ä
                case '\u00F6' -> "oe"; // ö
                case '\u00FC' -> "ue"; // ü
                case '\u00C4' -> "Ae"; // Ä
                case '\u00D6' -> "Oe"; // Ö
                case '\u00DC' -> "Ue"; // Ü
                case '\u00DF' -> "ss"; // ß
                default -> null;
            };
            if (expansion != null) {
                if (result == null) {
                    result = new StringBuilder(input.length() + 8);
                    result.append(input, 0, i);
                }
                result.append(expansion);
            } else if (result != null) {
                result.append(c);
            }
        }
        return result != null ? result.toString() : input;
    }

    private static String normalizeDigits(String text) {
        StringBuilder result = null;
        for (int offset = 0; offset < text.length();) {
            int point = text.codePointAt(offset);
            int digit = Character.digit(point, 10);
            if (digit >= 0 && point > '9') {
                if (result == null) {
                    result = new StringBuilder(text.length()).append(text, 0, offset);
                }
                result.append((char) ('0' + digit));
            } else if (result != null) {
                result.appendCodePoint(point);
            }
            offset += Character.charCount(point);
        }
        return result == null ? text : result.toString();
    }

    /**
     * Strip accent marks (combining diacritical marks) for "case" sensitivity. This allows comparison that considers
     * case but ignores accents.
     */
    private static String stripAccents(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        StringBuilder result = null;
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            // Skip combining diacritical marks (U+0300 - U+036F)
            if (c >= '\u0300' && c <= '\u036F') {
                if (result == null) {
                    result = new StringBuilder(normalized.length());
                    result.append(normalized, 0, i);
                }
            } else if (result != null) {
                result.append(c);
            }
        }
        return result != null ? result.toString() : normalized;
    }

    /**
     * Strip punctuation and whitespace characters for ignorePunctuation comparison.
     */
    private static String stripPunctuation(String input) {
        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int type = Character.getType(c);
            // Skip punctuation (Pc, Pd, Ps, Pe, Pi, Pf, Po) and separator (Zs, Zl, Zp) categories
            if (type != Character.CONNECTOR_PUNCTUATION && type != Character.DASH_PUNCTUATION
                    && type != Character.START_PUNCTUATION && type != Character.END_PUNCTUATION
                    && type != Character.INITIAL_QUOTE_PUNCTUATION && type != Character.FINAL_QUOTE_PUNCTUATION
                    && type != Character.OTHER_PUNCTUATION && type != Character.SPACE_SEPARATOR
                    && type != Character.LINE_SEPARATOR && type != Character.PARAGRAPH_SEPARATOR) {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static int textEnd(String value, int start) {
        int end = start + Character.charCount(value.codePointAt(start));
        while (end < value.length() && !Character.isDigit(value.codePointAt(end))) {
            end += Character.charCount(value.codePointAt(end));
        }
        return end;
    }
}

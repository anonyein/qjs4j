/*
 * Copyright (c) 2025-2026. caoccao.com Sam Cao
 */
package com.caoccao.qjs4j.compiler.ast;

import java.util.List;

/**
 * Represents a comma-separated sequence expression (comma operator).
 */
public record SequenceExpression(
        List<Expression> expressions,
        SourceLocation location
) implements Expression {
    @Override
    public SourceLocation getLocation() {
        return location;
    }
}

package com.resumelens.model;

import java.util.Set;

public record JobRequirements(Set<String> required, Set<String> preferred) {}

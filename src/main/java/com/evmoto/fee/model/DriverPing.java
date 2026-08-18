package com.evmoto.fee.model;

import java.time.OffsetDateTime;

public record DriverPing(OffsetDateTime at, double lat, double lng) {}

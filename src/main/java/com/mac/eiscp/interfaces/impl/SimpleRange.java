/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mac.eiscp.interfaces.Range;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mac
 */
@JsonInclude(Include.NON_NULL)
public class SimpleRange implements Range {

    private static final Logger log = LoggerFactory.getLogger(SimpleRange.class);

    private Integer min;
    private Integer max;
    private Integer step;
    private boolean isHex;

    private Integer current;

    public SimpleRange() {
        min = null;
        max = null;
        current = null;
        step = null;
        isHex = false;
    }

    public SimpleRange(Range range) {
        this();
        if (Objects.nonNull(range)) {
            this.min = range.getMin();
            this.max = range.getMax();
            this.current = range.getCurrent();
            this.step = range.getSteps();
            this.isHex = range.isHex();
        }
    }

    public SimpleRange(int min, int max) {
        this();
        this.min = min;
        this.max = max;
    }

    public SimpleRange(int min, int max, int step) {
        this();
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void setCurrent(int current) {
        if (Objects.nonNull(min) && Objects.nonNull(max)) {
            if (min > Integer.MIN_VALUE && max > Integer.MIN_VALUE) {
                Preconditions.checkArgument(current >= min);
                Preconditions.checkArgument(current <= max);
                this.current = current;
            }
        }
    }

    @Override
    public Integer getMin() {
        return this.min;
    }

    @Override
    public Integer getMax() {
        return this.max;
    }

    /**
     *
     * @return
     */
    @Override
    public Integer getCurrent() {
        return this.current;
    }

    @Override
    public Integer getSteps() {
        return this.step;
    }

    @Override
    public void setSteps(int steps) {
        if (steps < 1) {
            return;
        }
        Preconditions.checkArgument(steps < max);
        this.step = steps;
    }

    @Override
    public Integer increase() {
        if (Objects.nonNull(current)) {
            current = this.current + step <= max ? current + step : current;
        }
        return current;
    }

    @Override
    public Integer decrease() {
        if (Objects.nonNull(current)) {
            current = this.current - step >= min ? current - step : current;
        }
        return current;
    }

    @Override
    public String currentHex() {
        return Objects.isNull(current) ? "" : format(Integer.toHexString(current)).toUpperCase();
    }

    @Override
    public String formatCurrent() {
        return String.format("%2d", current);
    }

    @Override
    public int hashCode() {
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher()
                .putInt(Objects.hashCode(this.min)).putInt(Objects.hashCode(this.max))
                .putInt(Objects.hashCode(this.step)).hash();
        return hc.asInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleRange other = (SimpleRange) obj;

        return Objects.equals(this.min, other.min)
                && Objects.equals(this.max, other.max)
                && Objects.equals(this.step, other.step);
    }

    @Override
    public void setAsHex(boolean asHex) {
        this.isHex = asHex;
    }

    @JsonIgnore
    @Override
    public boolean isHex() {
        return isHex;
    }

    private String format(String value) {
        return String.format("%2s", value.trim()).replaceAll("\\s+", "0");
    }

}

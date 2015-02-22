package com.mac.eiscp.interfaces.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mac.eiscp.interfaces.Parameter;
import com.mac.eiscp.interfaces.Range;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 *
 * @author Mac
 */
public class EiscpParameter implements Parameter {

    private String paramName;
    private Range range;

    public EiscpParameter() {
        this.paramName = "";
        this.range = new SimpleRange(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
    
    public EiscpParameter(String param) {
        this();
        this.paramName = param;
    }
    
    public EiscpParameter(Parameter param){
        this();
        if(Objects.nonNull(param)){
            if(Objects.nonNull(param.getParamName())){
                this.paramName = param.getParamName();
            }
            if(Objects.nonNull(param.getRange())){
                this.range = new SimpleRange(param.getRange());
            }
        }
    }

    @Override
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public String getParamName() {
        return this.paramName;
    }

    @Override
    public void setRange(Range range) {
        this.range = range;
    }

    @Override
    public Range getRange() {
        return this.range;
    }

    @JsonIgnore
    @Override
    public String getParameter() {
        String param = null;
        if(Objects.nonNull(this.paramName) && this.range.getMax() > Integer.MIN_VALUE){
            param = this.paramName + this.range.currentHex();
        }else if(Objects.isNull(this.paramName)){
            param = this.range.currentHex();
        }else if(this.range.getMin() == Integer.MIN_VALUE && this.range.getMax() == Integer.MIN_VALUE){
            param = this.paramName;
        }
        return param;
    }

    @Override
    public int length() {
        return Objects.nonNull(this.paramName) && Objects.nonNull(this.range)
                && this.range.getMax() != Integer.MIN_VALUE
                ? this.paramName.length() + this.range.currentHex().length()
                : Objects.nonNull(this.paramName) ? this.paramName.length()
                        : this.range.currentHex().length();
    }

    @Override
    public int hashCode() {
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher()
                .putString(this.paramName, Charset.defaultCharset())
                .putInt(this.range.hashCode()).hash();
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
        final EiscpParameter other = (EiscpParameter) obj;
        return Objects.equals(this.paramName, other.paramName)
                && Objects.equals(this.range, other.range);
    }

}

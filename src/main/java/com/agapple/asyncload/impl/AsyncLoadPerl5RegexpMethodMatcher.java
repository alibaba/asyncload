package com.agapple.asyncload.impl;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadMethodMatch;
import com.agapple.asyncload.impl.exceptions.AsyncLoadException;

/**
 * 基于Perl5 oro进行正则匹配的matcher
 * 
 * @author jianghang 2011-1-21 下午10:40:44
 */
public class AsyncLoadPerl5RegexpMethodMatcher implements AsyncLoadMethodMatch {

    // 匹配字符串
    private String[]       patterns                  = new String[0];
    private String[]       excludedPatterns          = new String[0];
    // 匹配对象
    private Pattern[]      compiledPatterns          = new Pattern[0];
    private Pattern[]      compiledExclusionPatterns = new Pattern[0];
    private boolean        excludeOveride            = false;             // 是否排除条件优先
    private PatternMatcher matcher                   = new Perl5Matcher(); // 对应的Matcher

    public boolean matches(Method method) {
        String signatureString = method.getName();
        if (excludeOveride) {
            return matchesExclusionFirst(signatureString);
        } else {
            return matchesFirst(signatureString);
        }
    }

    /**
     * 优先采取pattern
     * 
     * @param signatureString
     * @return
     */
    private boolean matchesFirst(String signatureString) {
        // 优先采取pattern
        for (int i = 0; i < this.compiledPatterns.length; i++) {
            boolean matched = this.matcher.matches(signatureString, this.compiledPatterns[i]);
            if (matched) {// 如果匹配，再进行excludePattern过滤
                for (int j = 0; j < this.compiledExclusionPatterns.length; j++) {
                    boolean excluded = this.matcher.matches(signatureString, this.compiledExclusionPatterns[j]);
                    if (excluded) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 优先采取excludePattern
     * 
     * @param signatureString
     * @return
     */
    private boolean matchesExclusionFirst(String signatureString) {
        // 优先采取excludePattern
        boolean excluded = false;
        for (int i = 0; i < this.compiledExclusionPatterns.length; i++) {
            excluded |= this.matcher.matches(signatureString, this.compiledExclusionPatterns[i]);
        }

        return !excluded;
    }

    /**
     * 编译对应的pattern，返回oro的Pattern对象
     */
    private Pattern[] compilePatterns(String[] source) {
        Perl5Compiler compiler = new Perl5Compiler();
        Pattern[] destination = new Pattern[source.length];
        for (int i = 0; i < source.length; i++) {
            try {
                destination[i] = compiler.compile(source[i], Perl5Compiler.READ_ONLY_MASK);
            } catch (MalformedPatternException ex) {
                throw new AsyncLoadException(ex.getMessage());
            }
        }
        return destination;
    }

    // ===================== setter / getter ============================

    public void setExcludeOveride(boolean excludeOveride) {
        this.excludeOveride = excludeOveride;
    }

    public void setPattern(String pattern) {
        setPatterns(new String[] { pattern });
    }

    public void setPatterns(String[] patterns) {
        Assert.notEmpty(patterns, "'patterns' cannot be null or empty.");
        this.patterns = patterns;
        this.compiledPatterns = compilePatterns(patterns);
    }

    public void setExcludedPattern(String excludedPattern) {
        setExcludedPatterns(new String[] { excludedPattern });
    }

    public void setExcludedPatterns(String[] excludedPatterns) {
        Assert.notEmpty(excludedPatterns, "excludedPatterns must not be empty");
        this.excludedPatterns = excludedPatterns;
        this.compiledExclusionPatterns = compilePatterns(excludedPatterns);

    }

    @Override
    public String toString() {
        return "AsyncLoadPerl5RegexpMethodMatcher [excludeOveride=" + excludeOveride + ", excludedPatterns="
               + Arrays.toString(excludedPatterns) + ", patterns=" + Arrays.toString(patterns) + "]";
    }

}

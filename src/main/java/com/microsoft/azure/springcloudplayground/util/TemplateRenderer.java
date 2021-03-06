package com.microsoft.azure.springcloudplayground.util;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.Template;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TemplateRenderer {

    @Getter
    @Setter
    private boolean cache = true;

    private final Compiler mustache;

    private final ConcurrentMap<String, Template> templateCaches = new ConcurrentReferenceHashMap<>();

    public TemplateRenderer() {
        this(mustacheCompiler());
    }

    public TemplateRenderer(Compiler mustache) {
        this.mustache = mustache;
    }

    public String process(String name, Map<String, ?> model) {
        try {
            Template template = getTemplate(name);
            return template.execute(model);
        }
        catch (Exception e) {
            log.error("Cannot render: " + name, e);
            throw new IllegalStateException("Cannot render template", e);
        }
    }

    public String processFromString(String tempStr, Map<String, ?> model) {
        Template template = this.mustache.compile(tempStr);
        return template.execute(model);
    }

    public Template getTemplate(String name) {
        if (this.cache) {
            return this.templateCaches.computeIfAbsent(name, this::loadTemplate);
        }
        return loadTemplate(name);
    }

    protected Template loadTemplate(String name) {
        try {
            Reader template;
            template = this.mustache.loader.getTemplate(name);
            return this.mustache.compile(template);
        }
        catch (Exception e) {
            throw new IllegalStateException("Cannot load template " + name, e);
        }
    }

    private static Compiler mustacheCompiler() {
        return Mustache.compiler().withLoader(mustacheTemplateLoader());
    }

    private static TemplateLoader mustacheTemplateLoader() {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String prefix = "classpath:/templates/";
        Charset charset = Charset.forName("UTF-8");

        return name -> new InputStreamReader(resourceLoader.getResource(prefix + name).getInputStream(), charset);
    }

}

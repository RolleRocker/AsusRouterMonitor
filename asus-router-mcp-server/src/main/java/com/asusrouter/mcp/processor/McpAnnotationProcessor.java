package com.asusrouter.mcp.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Annotation processor for MCP annotations.
 * Processes @McpTool, @McpSchema, and @McpParameter annotations at compile-time
 * to generate type-safe tool schemas and the MCP tool registry.
 */
@SupportedAnnotationTypes({
    "com.asusrouter.mcp.annotations.McpTool",
    "com.asusrouter.mcp.annotations.McpSchema",
    "com.asusrouter.mcp.annotations.McpParameter"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class McpAnnotationProcessor extends AbstractProcessor {
    
    private static final String TOOL_SCHEMA_CLASS_NAME = "ToolSchema";
    
    private Filer filer;
    private Messager messager;
    private final Map<String, ToolMetadata> tools = new HashMap<>();
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }
    
    @Override
    @SuppressWarnings("all") // Always returns true per annotation processor contract
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            // Final round - generate registry and JSON file
            if (!tools.isEmpty()) {
                try {
                    generateToolRegistry();
                    generateMcpToolsJson();
                } catch (IOException e) {
                    error("Failed to generate MCP tool artifacts: " + e.getMessage());
                }
            }
            return true;
        }
        
        // Process @McpTool annotations
        for (Element element : roundEnv.getElementsAnnotatedWith(McpTool.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                error("@McpTool can only be applied to interfaces", element);
                continue;
            }
            
            processToolAnnotation((TypeElement) element);
        }
        
        return true;
    }
    
    private void processToolAnnotation(TypeElement element) {
        McpTool annotation = element.getAnnotation(McpTool.class);
        
        // Validate tool name
        String toolName = annotation.name();
        if (toolName.isEmpty()) {
            error("@McpTool name cannot be empty", element);
            return;
        }
        
        if (tools.containsKey(toolName)) {
            error("Duplicate tool name: " + toolName, element);
            return;
        }
        
        // Validate description
        String description = annotation.description();
        if (description.isEmpty()) {
            error("@McpTool description cannot be empty", element);
            return;
        }
        
        // Find execute method
        ExecutableElement executeMethod = findExecuteMethod(element);
        if (executeMethod == null) {
            error("@McpTool interface must have an 'execute' method", element);
            return;
        }
        
        // Build tool metadata
        ToolMetadata metadata = new ToolMetadata();
        metadata.name = toolName;
        metadata.description = description;
        metadata.errorCodes = Arrays.asList(annotation.errorCodes());
        metadata.interfaceElement = element;
        metadata.executeMethod = executeMethod;
        metadata.inputParameters = extractInputParameters(executeMethod);
        metadata.returnType = executeMethod.getReturnType();
        
        tools.put(toolName, metadata);
        
        // Generate individual tool schema class
        try {
            generateToolSchemaClass(metadata);
        } catch (IOException e) {
            error("Failed to generate tool schema for " + toolName + ": " + e.getMessage(), element);
        }
    }
    
    private ExecutableElement findExecuteMethod(TypeElement element) {
        for (Element enclosed : element.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) enclosed;
                if ("execute".equals(method.getSimpleName().toString())) {
                    return method;
                }
            }
        }
        return null;
    }
    
    private List<ParameterMetadata> extractInputParameters(ExecutableElement method) {
        List<ParameterMetadata> parameters = new ArrayList<>();
        
        for (VariableElement param : method.getParameters()) {
            ParameterMetadata metadata = new ParameterMetadata();
            metadata.name = param.getSimpleName().toString();
            metadata.type = param.asType();
            
            McpParameter annotation = param.getAnnotation(McpParameter.class);
            if (annotation != null) {
                metadata.required = annotation.required();
                metadata.pattern = annotation.pattern();
                metadata.min = annotation.min();
                metadata.max = annotation.max();
                metadata.description = annotation.description();
            }
            
            parameters.add(metadata);
        }
        
        return parameters;
    }
    
    private void generateToolSchemaClass(ToolMetadata metadata) throws IOException {
        String className = toSchemaClassName(metadata.name);
        String packageName = "com.asusrouter.infrastructure.adapters.in.mcp.generated";
        
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ClassName.get(packageName, TOOL_SCHEMA_CLASS_NAME))
            .addJavadoc("Generated tool schema for $L\n", metadata.name)
            .addJavadoc("Generated by McpAnnotationProcessor\n");
        
        // Add constants
        classBuilder.addField(
            FieldSpec.builder(String.class, "NAME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", metadata.name)
                .build()
        );
        
        classBuilder.addField(
            FieldSpec.builder(String.class, "DESCRIPTION", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", metadata.description)
                .build()
        );
        
        // Implement interface methods
        classBuilder.addMethod(
            MethodSpec.methodBuilder("getName")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String.class)
                .addStatement("return NAME")
                .build()
        );
        
        classBuilder.addMethod(
            MethodSpec.methodBuilder("getDescription")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String.class)
                .addStatement("return DESCRIPTION")
                .build()
        );
        
        // Future enhancement: Add getInputSchema(), getOutputSchema(), getErrors() methods
        // when full MCP schema generation is implemented
        
        TypeSpec typeSpec = classBuilder.build();
        
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
            .addFileComment("AUTO-GENERATED by McpAnnotationProcessor - DO NOT EDIT")
            .build();
        
        javaFile.writeTo(filer);
    }
    
    private void generateToolRegistry() throws IOException {
        String packageName = "com.asusrouter.infrastructure.adapters.in.mcp.generated";
        
        TypeSpec.Builder registryBuilder = TypeSpec.classBuilder("McpToolRegistry")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc("Generated registry of all MCP tools\n")
            .addJavadoc("Generated by McpAnnotationProcessor\n");
        
        // Add static map field
        ClassName mapType = ClassName.get("java.util", "Map");
        ClassName stringType = ClassName.get(String.class);
        ClassName toolSchemaType = ClassName.get(packageName, TOOL_SCHEMA_CLASS_NAME);
        TypeName mapTypeName = ParameterizedTypeName.get(mapType, stringType, toolSchemaType);
        
        CodeBlock.Builder staticInitBuilder = CodeBlock.builder()
            .addStatement("$T<String, " + TOOL_SCHEMA_CLASS_NAME + "> map = new $T<>()", Map.class, HashMap.class);
        
        for (String toolName : tools.keySet()) {
            String schemaClassName = toSchemaClassName(toolName);
            staticInitBuilder.addStatement("map.put($S, new $L())", toolName, schemaClassName);
        }
        
        staticInitBuilder.addStatement("TOOLS = $T.unmodifiableMap(map)", Collections.class);
        
        registryBuilder.addField(
            FieldSpec.builder(mapTypeName, "TOOLS", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(staticInitBuilder.build())
                .build()
        );
        
        // Add getAllTools method
        registryBuilder.addMethod(
            MethodSpec.methodBuilder("getAllTools")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(mapTypeName)
                .addStatement("return TOOLS")
                .build()
        );
        
        // Add getTool method
        registryBuilder.addMethod(
            MethodSpec.methodBuilder("getTool")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "name")
                .returns(toolSchemaType)
                .addStatement("return TOOLS.get(name)")
                .build()
        );
        
        TypeSpec typeSpec = registryBuilder.build();
        
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
            .addFileComment("AUTO-GENERATED by McpAnnotationProcessor - DO NOT EDIT")
            .build();
        
        javaFile.writeTo(filer);
    }
    
    private void generateMcpToolsJson() throws IOException {
        FileObject resource = filer.createResource(
            StandardLocation.CLASS_OUTPUT,
            "",
            "generated/mcp-tools.json"
        );
        
        try (Writer writer = resource.openWriter()) {
            writer.write("{\n");
            writer.write("  \"tools\": [\n");
            
            int i = 0;
            for (ToolMetadata metadata : tools.values()) {
                writer.write("    {\n");
                writer.write("      \"name\": \"" + metadata.name + "\",\n");
                writer.write("      \"description\": \"" + escapeJson(metadata.description) + "\"\n");
                writer.write("    }");
                
                if (++i < tools.size()) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            
            writer.write("  ]\n");
            writer.write("}\n");
        }
    }
    
    private String toSchemaClassName(String toolName) {
        // Convert asus_router_get_uptime -> GetUptime + TOOL_SCHEMA_CLASS_NAME
        String[] parts = toolName.split("_");
        StringBuilder className = new StringBuilder();
        
        // Skip "asus" and "router" prefix
        int startIndex = 0;
        if (parts.length > 0 && parts[0].equals("asus")) startIndex++;
        if (parts.length > 1 && parts[1].equals("router")) startIndex++;
        
        for (int i = startIndex; i < parts.length; i++) {
            className.append(capitalize(parts[i]));
        }
        className.append(TOOL_SCHEMA_CLASS_NAME);
        
        return className.toString();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }
    
    private void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
    
    // Inner classes for metadata - fields reserved for future schema generation functionality
    @SuppressWarnings("unused")
    private static class ToolMetadata {
        String name;
        String description;
        List<String> errorCodes;
        TypeElement interfaceElement;
        ExecutableElement executeMethod;
        List<ParameterMetadata> inputParameters;
        TypeMirror returnType;
    }
    
    @SuppressWarnings("unused")
    private static class ParameterMetadata {
        String name;
        TypeMirror type;
        boolean required = true;
        String pattern = "";
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;
        String description = "";
    }
}

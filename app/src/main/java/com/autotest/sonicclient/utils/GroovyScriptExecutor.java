package com.autotest.sonicclient.utils;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

public class GroovyScriptExecutor {

    /**
     * 执行 Groovy 脚本字符串
     * @param scriptText Groovy 脚本文本
     * @return 脚本执行结果
     */
    public static Object executeScript(String scriptText) {
        try {
            // 创建更兼容的配置
            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.setTargetBytecode(CompilerConfiguration.JDK8);

            // 使用应用的类加载器
            ClassLoader parentClassLoader = GroovyScriptExecutor.class.getClassLoader();
            Binding binding = new Binding();

            // 创建 GroovyShell 实例
            GroovyShell shell = new GroovyShell(parentClassLoader, binding, configuration);
            return shell.evaluate(scriptText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行 Groovy 脚本字符串并传入参数
     * @param scriptText Groovy 脚本文本
     * @param params 参数映射
     * @return 脚本执行结果
     */
    public static Object executeScript(String scriptText, java.util.Map<String, Object> params) {
        try {
            // 创建更兼容的配置
            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.setTargetBytecode(CompilerConfiguration.JDK8);

            // 使用应用的类加载器
            ClassLoader parentClassLoader = GroovyScriptExecutor.class.getClassLoader();
            Binding binding = new Binding();

            // 添加参数到绑定
            if (params != null) {
                for (java.util.Map.Entry<String, Object> entry : params.entrySet()) {
                    binding.setVariable(entry.getKey(), entry.getValue());
                }
            }

            // 创建 GroovyShell 实例
            GroovyShell shell = new GroovyShell(parentClassLoader, binding, configuration);
            return shell.evaluate(scriptText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
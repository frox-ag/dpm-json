package com.frox.opendpm.processtools.json.integration

import com.frox.opendpm.processtools.json.JsonProxyUtils
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import javax.script.ScriptEngine

@Component
class CamundaImportCustomizer {

    protected static final String IMPORT_ALIAS = 'dpmJson'
    protected static final String GROOVY_SCRIPTING_LANGUAGE = 'groovy'

    private final ProcessEngineFactoryBean processEngineFactory;

    CamundaImportCustomizer(ProcessEngineFactoryBean processEngineFactory) {
        this.processEngineFactory = processEngineFactory
    }

    @EventListener
    private void configureGroovy(ProcessApplicationStartedEvent event) {
        ProcessEngineConfigurationImpl processEngineConfiguration = processEngineFactory.getProcessEngineConfiguration()
        ScriptingEngines scriptingEngines = processEngineConfiguration.getScriptingEngines()
        ScriptEngine scriptEngine = (ScriptEngine) processEngineConfiguration.getCommandExecutorTxRequired()
                .execute(new Command<Object>() {
                    ScriptEngine execute(CommandContext commandContext) {
                        return scriptingEngines.getScriptEngineForLanguage(GROOVY_SCRIPTING_LANGUAGE)
                    }
                })
        if (scriptEngine instanceof GroovyScriptEngineImpl) {
            GroovyScriptEngineImpl groovyScriptEngine = (GroovyScriptEngineImpl) scriptEngine
            GroovyClassLoader classLoader = groovyScriptEngine.getClassLoader()
            ImportCustomizer dpmCustomizer = new ImportCustomizer()
            dpmCustomizer.addStaticImport(IMPORT_ALIAS, JsonProxyUtils.class.name, 'wrap')
            CompilerConfiguration compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
            compilerConfiguration.getCompilationCustomizers().add(dpmCustomizer)
            groovyScriptEngine.setClassLoader(new GroovyClassLoader(classLoader, compilerConfiguration))
        }
    }
}

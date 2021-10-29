package com.frox.opendpm.processtools.json.integration

import com.frox.opendpm.processtools.json.JsonProxyUtils
import com.frox.opendpm.processtools.json.SpinJsonNodeProxy
import org.springframework.stereotype.Component

/**
 * Provides Spring component - entry point for wrapping input objects
 * Available for every script section in Camunda that supports Groovy by id.
 *
 * @author <a href="mailto:opendpm@frox.ch">Open Dpm</a>
 */
@Component("dpmJson")
class DpmJsonProcessTool {

    /**
     * Create an empty wrapped object.
     *
     * @return wrapped object
     * @since 1.0.0
     */
    SpinJsonNodeProxy wrap() {
        return JsonProxyUtils.wrap()
    }

    /**
     * Wrap input object.
     * If input object is null - will create empty wrapped object.
     * Works with collections, maps, strings and any kinds of objects, which can be transformed to {@link org.camunda.spin.json.SpinJsonNode}
     *
     * @param input object
     * @return wrapped object
     * @since 1.0.0
     */
    SpinJsonNodeProxy wrap(Object json) {
        return JsonProxyUtils.wrap(json)
    }
}

package com.frox.opendpm.processtools.json.integration

import com.frox.opendpm.processtools.json.JsonProxyUtils
import com.frox.opendpm.processtools.json.SpinJsonNodeProxy
import org.springframework.stereotype.Component

@Component("dpmJson")
class DpmJsonProcessTool {

    SpinJsonNodeProxy wrap() {
        return JsonProxyUtils.wrap()
    }

    SpinJsonNodeProxy wrap(Object json) {
        return JsonProxyUtils.wrap(json)
    }
}

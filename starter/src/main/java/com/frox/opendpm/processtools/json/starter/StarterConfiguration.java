package com.frox.opendpm.processtools.json.starter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Spring components, defines project packages to scan.
 *
 * @author <a href="mailto:opendpm@frox.ch">Open Dpm</a>
 */
@Configuration("dpmPJsonStarterConfiguration")
@ComponentScan(basePackages = {"com.frox.opendpm.processtools.json"})
public class StarterConfiguration {

}

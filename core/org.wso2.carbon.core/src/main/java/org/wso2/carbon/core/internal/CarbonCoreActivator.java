/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.lang.management.ManagementPermission;
import java.security.Provider;
import java.security.Security;

/**
 * OSGi bundle Activator for Carbon core
 */
public class CarbonCoreActivator implements BundleActivator {

    private static final Log log = LogFactory.getLog(CarbonCoreActivator.class);
    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    public void start(BundleContext context) throws Exception {
        // Need permissions in order to activate Carbon Core
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
           secMan.checkPermission(new ManagementPermission("control"));
        }
        // We assume it's super tenant during the deployment time
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        dataHolder.setBundleContext(context);
        log.info("Starting WSO2 Carbon...");
        log.info("Operating System : " + System.getProperty("os.name") + " " +
                 System.getProperty("os.version") + ", " + System.getProperty("os.arch"));
        log.info("Java Home        : " + System.getProperty("java.home"));
        log.info("Java Version     : " + System.getProperty("java.version"));
        log.info("Java VM          : " + System.getProperty("java.vm.name") + " " +
                 System.getProperty("java.vm.version") +
                 "," +
                 System.getProperty("java.vendor"));

        String carbonHome;
        if ((carbonHome = System.getProperty("carbon.home")).equals(".")) {
            carbonHome = new File(".").getAbsolutePath();
        }

        log.info("Carbon Home      : " + carbonHome);
        log.info("Java Temp Dir    : " + System.getProperty("java.io.tmpdir"));
        log.info("User             : " + System.getProperty("user.name") + ", " +
                 System.getProperty("user.language") + "-" + System.getProperty("user.country") +
                 ", " + System.getProperty("user.timezone"));

        String cryptoProviderIdentifier = getPreferredJceProviderIdentifier();
        String cryptoProviderClass = getPreferredJceProviderClass(cryptoProviderIdentifier);
        Security.addProvider((Provider) Class.forName(cryptoProviderClass).getDeclaredConstructor().newInstance());
        if(log.isDebugEnabled()){
            log.debug(cryptoProviderClass + " security provider is successfully registered in JVM.");
        }
    }

    public void stop(BundleContext context) throws Exception {
        dataHolder.setBundleContext(null);
    }

    /**
     * This method returns the preferred JCE provider identifier to be used.
     *
     * @return jce provider identifier name
     */
    private static String getPreferredJceProviderIdentifier() {
        String provider = System.getProperty(ServerConstants.JCE_PROVIDER_PARAMETER);
        if (ServerConstants.BOUNCY_CASTLE_FIPS_PROVIDER_IDENTIFIER.equalsIgnoreCase(provider)) {
            return ServerConstants.BOUNCY_CASTLE_FIPS_PROVIDER_IDENTIFIER;
        }
        return ServerConstants.BOUNCY_CASTLE_PROVIDER_IDENTIFIER;
    }

    /**
     * This method returns the preferred JCE provider class to be used.
     *
     * @return jce provider identifier name
     */
    private static String getPreferredJceProviderClass(String providerIdentifier) {
        if (ServerConstants.BOUNCY_CASTLE_PROVIDER_IDENTIFIER.equalsIgnoreCase(providerIdentifier)) {
            return ServerConstants.BOUNCY_CASTLE_PROVIDER_CLASS;
        } else if (ServerConstants.BOUNCY_CASTLE_FIPS_PROVIDER_IDENTIFIER.equalsIgnoreCase(providerIdentifier)) {
            return ServerConstants.BOUNCY_CASTLE_FIPS_PROVIDER_CLASS;
        } else {
            throw new IllegalArgumentException("Unsupported JCE provider: " + providerIdentifier);
        }
    }
}

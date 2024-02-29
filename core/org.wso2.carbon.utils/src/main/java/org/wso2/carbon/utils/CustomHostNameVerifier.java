/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.conn.ssl.AbstractVerifier;

import javax.net.ssl.SSLException;

/**
 * Custom hostname verifier class to allow Default and localhost behavior.
 */
public class CustomHostNameVerifier extends AbstractVerifier {

    private static final String[] LOCAL_HOSTS = {"::1", "127.0.0.1", "localhost", "localhost.localdomain"};

    @Override
    public void verify(String hostname, String[] commonNames, String[] subjectAlternativeNames) throws SSLException {

        String[] subjectAltsWithLocalhosts = (String[]) ArrayUtils.addAll(subjectAlternativeNames, LOCAL_HOSTS);

        if (ArrayUtils.isNotEmpty(commonNames) && !ArrayUtils.contains(subjectAlternativeNames, commonNames[0])) {
            subjectAltsWithLocalhosts = (String[]) ArrayUtils.add(subjectAltsWithLocalhosts, commonNames[0]);
        }
        super.verify(hostname, commonNames, subjectAltsWithLocalhosts, false);
    }
}
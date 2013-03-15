/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.gf.ejb;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.spi.ExceptionMappers;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

/**
 * Helper class to handle exceptions wrapped by the EJB container with EJBException.
 * If this mapper was not registered, no {@link WebApplicationException}
 * would end up mapped to the corresponding response.
 *
 * @author Paul Sandoz (paul.sandoz at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class EjbExceptionMapper implements ExtendedExceptionMapper<EJBException> {

    private final Provider<ExceptionMappers> mappers;

    /**
     * Create new EJB exception mapper.
     *
     * @param mappers utility to find mapper delegate.
     */
    @Inject
    public EjbExceptionMapper(Provider<ExceptionMappers> mappers) {
        this.mappers = mappers;
    }

    @Override
    public Response toResponse(EJBException exception) {
        return causeToResponse(exception);
    }

    @Override
    public boolean isMappable(EJBException exception) {
        try {
            return (causeToResponse(exception) != null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Response causeToResponse(EJBException exception) {
        final Exception cause = exception.getCausedByException();
        if (cause != null) {
            final ExceptionMapper mapper = mappers.get().findMapping(cause);
            if (mapper != null && mapper != this) {
                return mapper.toResponse(cause);
            }
        }
        return null;
    }
}

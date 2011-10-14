/**
 * Copyright (c) 2011, Qulice.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the Qulice.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.qulice.maven;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.context.Context;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test case for {@link CheckMojo} class.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CheckMojo.class, ValidatorsProvider.class })
public final class CheckMojoTest extends AbstractMojoTestCase {

    /**
     * Skip option should work.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void testSkipOption() throws Exception {
        final CheckMojo mojo = new CheckMojo();
        final Log log = Mockito.mock(Log.class);
        mojo.setLog(log);
        mojo.setSkip(true);
        mojo.execute();
        Mockito.verify(log).info("Execution skipped");
    }

    /**
     * Validation should happen.
     * @throws Exception If something wrong happens inside
     * @checkstyle ExecutableStatementCount (30 lines)
     */
    @Test
    public void testValidatingWorks() throws Exception {
        PowerMockito.mockStatic(ValidatorsProvider.class);
        final ValidatorsProvider factory =
            PowerMockito.mock(ValidatorsProvider.class);
        final List<Validator> validators = new ArrayList<Validator>();
        final CheckMojoTest.SpyValidator validator =
            new CheckMojoTest.SpyValidator();
        validators.add(validator);
        Mockito.doReturn(validators).when(factory).all();
        PowerMockito.whenNew(ValidatorsProvider.class).withNoArguments()
            .thenReturn(factory);
        final CheckMojo mojo = new CheckMojo();
        final MavenProject project = Mockito.mock(MavenProject.class);
        mojo.setProject(project);
        mojo.setLog(Mockito.mock(Log.class));
        final String license = "file:./some-file.txt";
        mojo.setLicense(license);
        final Context context = Mockito.mock(Context.class);
        mojo.contextualize(context);
        mojo.execute();
        Mockito.verify(factory).all();
        final Environment env = validator.env();
        MatcherAssert.assertThat(env, Matchers.notNullValue());
        MatcherAssert.assertThat(env.project(), Matchers.equalTo(project));
        MatcherAssert.assertThat(env.context(), Matchers.equalTo(context));
        MatcherAssert.assertThat(
            env.properties().getProperty("license"),
            Matchers.equalTo(license)
        );
    }

    /**
     * Simulator of validation.
     */
    private static final class SpyValidator implements Validator {
        /**
         * Environment.
         */
        private Environment env;
        /**
         * {@inheritDoc}
         */
        @Override
        public void validate(final Environment environment) {
            this.env = environment;
        }
        /**
         * Return the environment.
         * @return The environment
         */
        public Environment env() {
            return this.env;
        }
    }

}
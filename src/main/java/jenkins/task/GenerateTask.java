/**
 * Copyright (c) 2018 MicroNova AG
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * <p>
 * 3. Neither the name of MicroNova AG nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jenkins.task;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Executor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import jenkins.internal.ClientRequest;
import jenkins.internal.Util;
import jenkins.internal.data.ApiVersion;
import jenkins.internal.data.GenerateConfiguration;
import jenkins.internal.data.ModelConfiguration;
import jenkins.internal.descriptor.ExamModelDescriptorTask;
import jenkins.plugins.exam.config.ExamModelConfig;
import jenkins.task._exam.Messages;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exam Generate Testcase Task
 */
public class GenerateTask extends Task implements SimpleBuildStep {

    private static final long serialVersionUID = 2641743348736414442L;

    /**
     * the modelConfiguration as ID, UUID, or FullScopedName
     */
    private String modelConfiguration;
    /**
     * Identifies {@link jenkins.plugins.exam.config.ExamModelConfig} to be used.
     */
    private String examModel;

    /**
     * {@link jenkins.internal.data.GenerateConfiguration} properties.
     */
    private String element;
    private String descriptionSource;
    private boolean documentInReport;
    private String errorHandling;
    private String frameFunctions;
    private String mappingList;
    private String testCaseStates;
    private String variant;

    public String getElement() {
        return element;
    }

    @DataBoundSetter
    public void setElement(String element) {
        this.element = element;
    }

    public String getDescriptionSource() {
        return descriptionSource;
    }

    @DataBoundSetter
    public void setDescriptionSource(String descriptionSource) {
        this.descriptionSource = descriptionSource;
    }

    public boolean isDocumentInReport() {
        return documentInReport;
    }

    @DataBoundSetter
    public void setDocumentInReport(boolean documentInReport) {
        this.documentInReport = documentInReport;
    }

    public String getErrorHandling() {
        return errorHandling;
    }

    @DataBoundSetter
    public void setErrorHandling(String errorHandling) {
        this.errorHandling = errorHandling;
    }

    public String getFrameFunctions() {
        return frameFunctions;
    }

    @DataBoundSetter
    public void setFrameFunctions(String frameFunctions) {
        this.frameFunctions = frameFunctions;
    }

    public String getMappingList() {
        return mappingList;
    }

    @DataBoundSetter
    public void setMappingList(String mappingList) {
        this.mappingList = mappingList;
    }

    public String getTestCaseStates() {
        return testCaseStates;
    }

    @DataBoundSetter
    public void setTestCaseStates(String testCaseStates) {
        this.testCaseStates = testCaseStates;
    }

    public String getVariant() {
        return variant;
    }

    @DataBoundSetter
    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getExamModel() {
        return examModel;
    }

    @DataBoundSetter
    public void setExamModel(String examModel) {
        this.examModel = examModel;
    }

    public String getModelConfiguration() {
        return modelConfiguration;
    }

    @DataBoundSetter
    public void setModelConfiguration(String modelConfiguration) {
        this.modelConfiguration = modelConfiguration;
    }

    /**
     * Constructor
     *
     * @param examModel          examModel
     * @param modelConfiguration modelConfiguration
     * @param element            element
     * @param descriptionSource  descriptionSource
     * @param documentInReport   documentInReport
     * @param errorHandling      errorHandling
     * @param frameFunctions     frameFunctions
     * @param mappingList        mappingList
     * @param testCaseStates     testCaseStates
     * @param variant            variant
     */
    @DataBoundConstructor
    public GenerateTask(String examModel, String examName, String modelConfiguration, String element, String descriptionSource,
                        boolean documentInReport, String errorHandling, String frameFunctions, String mappingList, String testCaseStates, String variant) {
        this.examModel = examModel;
        this.examName = examName;
        this.modelConfiguration = modelConfiguration;

        this.element = element;
        this.descriptionSource = descriptionSource;
        this.documentInReport = documentInReport;
        this.errorHandling = errorHandling;
        this.variant = variant;

        this.frameFunctions = frameFunctions;
        this.mappingList = mappingList;
        this.testCaseStates = testCaseStates;
    }

    @Override
    protected void doExecuteTask(ClientRequest clientRequest) throws IOException, InterruptedException {
        if (clientRequest.isClientConnected()) {
            ModelConfiguration modelConfig = createModelConfig();
            GenerateConfiguration generateConfiguration = createGenerateConfig();

            clientRequest.createExamProject(modelConfig);
            clientRequest.generateTestcases(generateConfiguration);
        }
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        Executor runExecutor = run.getExecutor();
        assert runExecutor != null;

        // prepare environment
        getTaskHelper().setRun(run);
        getTaskHelper().setWorkspace(workspace);
        getTaskHelper().setLauncher(launcher);
        getTaskHelper().setTaskListener(listener);

        getTaskHelper().perform(this, launcher, new ApiVersion(1, 0, 3));
    }

    private GenerateConfiguration createGenerateConfig() {
        GenerateConfiguration configuration = new GenerateConfiguration();
        configuration.setElement(getElement());
        configuration.setDescriptionSource(getDescriptionSource());
        configuration.setDocumentInReport(isDocumentInReport());
        configuration.setErrorHandling(getErrorHandling());
        configuration.setFrameFunctions(convertToList(getFrameFunctions()));
        configuration.setMappingList(convertToList(getMappingList()));
        configuration.setTestCaseStates(convertToList(getTestCaseStates()));
        configuration.setVariant(getVariant());

        return configuration;
    }

    private ModelConfiguration createModelConfig() throws AbortException {
        ModelConfiguration mc = new ModelConfiguration();
        ExamModelConfig m = getModel(examModel);
        if (m == null) {
            throw new AbortException("ERROR: no model configured with name: " + examModel);
        }
        mc.setProjectName(m.getName());
        mc.setModelName(m.getModelName());
        mc.setTargetEndpoint(m.getTargetEndpoint());
        mc.setModelConfigUUID(modelConfiguration);

        return mc;
    }

    private List<String> convertToList(String list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        String[] split = list.split(",");
        return Arrays.asList(split);
    }

    /**
     * The Descriptor of the GenerateTask
     */
    @Extension
    @Symbol("examGenerate")
    public static class DescriptorGenerateTask extends ExamModelDescriptorTask {
        /**
         * @return the EXAM Groovy display name
         */
        @Nonnull
        public String getDisplayName() {
            return Messages.EXAM_GenerateTask();
        }

        /**
         * Constructor of this Descriptor
         */
        public DescriptorGenerateTask() {
            load();
        }

        /**
         * Checks if the Element is a valid EXAM ID,UUID or FSN.
         *
         * @param value value
         * @return If the form is ok
         */
        public FormValidation doCheckElement(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }
            return Util.validateElementForSearch(value);
        }

        /**
         * Checks if the Input is a valid descriptionSource
         *
         * @param value value
         * @return If the form is ok
         */
        public FormValidation doCheckDescriptionSource(@QueryParameter String value) {
            List<String> possibleValues = Arrays.asList("BESCHREIBUNG", "DESCRIPTION", "");
            if (possibleValues.contains(value)) {
                return FormValidation.ok();
            }
            return FormValidation.error("Value is not valid");
        }

        /**
         * Checks if the Input is a valid errorHandling argument.
         *
         * @param value value
         * @return If the form is ok
         */
        public FormValidation doCheckErrorHandling(@QueryParameter String value) {
            List<String> possibleValues = Arrays.asList("GENERATE_ERROR_STEP", "SKIP_TESTCASE", "ABORT", "");
            if (possibleValues.contains(value)) {
                return FormValidation.ok();
            }
            return FormValidation.error("Value is not valid");
        }

        /**
         * Checks if the Input is a valid frameFunction.
         *
         * @param value value
         * @return If the form is ok
         */
        public FormValidation doCheckFrameFunctions(@QueryParameter String value) {
            List<String> possibleValues = Arrays.asList("PRECONDITION_BEFORE", "PRECONDITION_AFTER", "ACTION_BEFORE", "ACTION_AFTER", "POSTCONDITION_BEFORE", "POSTCONDITION_AFTER", "EXPECTED_RESULT_BEFORE", "EXPECTED_RESULT_AFTER", "NUMBERED_FRAME_STEP");
            return Util.checkIfStringContainsValues(possibleValues, ",", value);
        }

        /**
         * Checks if the mappingList is valid.
         *
         * @param value value
         * @return If the form is ok
         */
        public FormValidation doCheckMappingList(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }
            return Util.validateElementForSearch(value);
        }

        /**
         * Checks if the testCaseStates are valid
         *
         * @param value value
         * @return If the form is ok
         */
        public FormValidation doCheckTestCaseStates(@QueryParameter String value) {
            List<String> possibleValues = Arrays.asList("NOT_YET_SPECIFIED", "SPECIFIED", "REVIEWED", "NOT_YET_IMPLEMENTED", "IMPLEMENTED", "PRODUCTIVE", "INVALID");
            return Util.checkIfStringContainsValues(possibleValues, ",", value);
        }

        /**
         * checks if the variant is valid
         *
         * @param value value
         * @return If the form is ok
         */
        public FormValidation doCheckVariant(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }
            return Util.validateElementForSearch(value);
        }
    }
}

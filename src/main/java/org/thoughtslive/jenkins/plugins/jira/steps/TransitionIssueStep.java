package org.thoughtslive.jenkins.plugins.jira.steps;

import static org.thoughtslive.jenkins.plugins.jira.util.Common.buildErrorResponse;

import hudson.Extension;
import hudson.Util;
import java.io.IOException;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.jira.api.ResponseData;
import org.thoughtslive.jenkins.plugins.jira.util.JiraStepDescriptorImpl;
import org.thoughtslive.jenkins.plugins.jira.util.JiraStepExecution;

/**
 * Step to transition JIRA issue.
 *
 * @author Naresh Rayapati
 */
public class TransitionIssueStep extends BasicJiraStep {

  private static final long serialVersionUID = 5648167982018270684L;

  @Getter
  private final String idOrKey;

  @Getter
  private final Object input;

  @DataBoundConstructor
  public TransitionIssueStep(final String idOrKey, final Object input) {
    this.idOrKey = idOrKey;
    this.input = input;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new Execution(this, context);
  }

  @Extension
  public static class DescriptorImpl extends JiraStepDescriptorImpl {

    @Override
    public String getFunctionName() {
      return "jiraTransitionIssue";
    }

    @Override
    public String getDisplayName() {
      return getPrefix() + "Transition Issue";
    }

  }

  public static class Execution extends JiraStepExecution<ResponseData<Void>> {

    private static final long serialVersionUID = 6038231959460139190L;

    private final TransitionIssueStep step;

    protected Execution(final TransitionIssueStep step, final StepContext context)
        throws IOException, InterruptedException {
      super(context);
      this.step = step;
    }

    @Override
    protected ResponseData<Void> run() throws Exception {

      ResponseData<Void> response = verifyInput();

      if (response == null) {
        logger.println(
            "JIRA: Site - " + siteName + " - Transition issue with idOrKey: " + step.getIdOrKey());
        response = jiraService.transitionIssue(step.getIdOrKey(), step.getInput());
      }

      return logResponse(response);
    }

    @Override
    protected <T> ResponseData<T> verifyInput() throws Exception {
      String errorMessage = null;
      ResponseData<T> response = verifyCommon(step);

      if (response == null) {
        final String idOrKey = Util.fixEmpty(step.getIdOrKey());

        if (idOrKey == null) {
          errorMessage = "idOrKey is empty or null.";
        }

        if (step.getInput() == null) {
          errorMessage = "input is null.";
          return buildErrorResponse(new RuntimeException(errorMessage));
        }

        if (errorMessage != null) {
          response = buildErrorResponse(new RuntimeException(errorMessage));
        }
      }
      return response;
    }
  }
}

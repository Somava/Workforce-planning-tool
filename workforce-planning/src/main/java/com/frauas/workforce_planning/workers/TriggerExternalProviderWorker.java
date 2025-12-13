package com.frauas.workforce_planning.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TriggerExternalProviderWorker {

  @JobWorker(type = "serviceMgmtConnector")
  public void triggerExternalProvider(final JobClient client, final ActivatedJob job) {

    Map<String, Object> vars = job.getVariablesAsMap();

    // 1) Ensure we have a stable correlation id for THIS instance
    String requestId = (String) vars.get("requestId");
    if (requestId == null || requestId.isBlank()) {
      requestId = "REQ-" + job.getProcessInstanceKey();  // stable + unique per instance
    }

    // 2) (Optional) record that we asked the external system
    Map<String, Object> out = new HashMap<>();
    out.put("requestId", requestId);
    out.put("externalRequestSent", true);

    System.out.println("[TriggerExternalProvider] Sent external request. requestId=" + requestId);

    // 3) Complete service task (process will now WAIT at message catch)
    client.newCompleteCommand(job.getKey())
          .variables(out)
          .send()
          .join();
  }
}

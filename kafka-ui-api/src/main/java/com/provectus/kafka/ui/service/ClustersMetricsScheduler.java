package com.provectus.kafka.ui.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Log4j2
public class ClustersMetricsScheduler {

  private final ClustersStorage clustersStorage;

  private final MetricsUpdateService metricsUpdateService;

  @Scheduled(fixedRateString = "${updateMetricsRateMillis:30000}")
  public void updateMetrics() {
    Flux.fromIterable(clustersStorage.getKafkaClustersMap().entrySet())
        .parallel()
        .runOn(Schedulers.parallel())
        .map(Map.Entry::getValue)
        .flatMap(metricsUpdateService::updateMetrics)
        .doOnNext(s -> clustersStorage.setKafkaCluster(s.getName(), s))
        .then()
        .block();
  }
}

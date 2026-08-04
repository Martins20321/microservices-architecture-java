package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.Map;

public class PagamentosServiceStack extends Stack {
    public PagamentosServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public PagamentosServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        IRepository iRepository = Repository.fromRepositoryName(this, id, "joseaws07/pagamentos-ms");

        ApplicationLoadBalancedFargateService fargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "MyFargateService")
                .serviceName("pagamentos-service-fargate")
                .cluster(cluster)           // Required
                .cpu(512)                   // Default is 256
                .desiredCount(1)            // Default is 1
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromEcrRepository(iRepository))
                                .containerName("PagamentosMs")
                                .containerPort(8080)
                                .environment(Map.of(
                                        "SPRING_DATA_MONGODB_URI", "mongodb://" +
                                                Fn.importValue("pagamentos-db-endpoint") + ":27017/pagamentos-ms",
                                        "SPRING_DATA_MONGODB_USERNAME", "pagamentos_admin",
                                        "SPRING_DATA_MONGODB_PASSWORD", Fn.importValue("pagamentos-db-password")

                                ))
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, "PagamentosMsLogGroup")
                                                .logGroupName("PagamentosMs")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix("PagamentosMs")
                                        .build()))
                                .build())
                .memoryLimitMiB(1024)       // Default is 512
                .publicLoadBalancer(true)   // Default is true
                .build();

        //Auto Scaling (Capacidade de aumentar ou diminuir o número de instâncias automaticamente conforme a demanda)
        ScalableTaskCount scalableTarget = fargateService.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(1)
                .maxCapacity(3)
                .build());
        //Escalando baseado na CPU
        scalableTarget.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(70)
                .scaleOutCooldown(Duration.minutes(2)) //Tempo par subir uma nova instância
                .scaleInCooldown(Duration.minutes(3))  //Tempo para destruir uma instância
                .build());
        //Escalando baseado na Memória
        scalableTarget.scaleOnMemoryUtilization("MemoryScaling", MemoryUtilizationScalingProps.builder()
                .targetUtilizationPercent(65)
                .scaleOutCooldown(Duration.minutes(2))
                .scaleInCooldown(Duration.minutes(3))
                .build());
    }
}

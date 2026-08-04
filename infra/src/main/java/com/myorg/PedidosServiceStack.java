package com.myorg;

import org.jetbrains.annotations.NotNull;
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

public class PedidosServiceStack extends Stack {
    public PedidosServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public PedidosServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        IRepository repository = Repository.fromRepositoryName(this, "repository", "joseaws07/pedidos-ms");

        ApplicationLoadBalancedFargateService fargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "PedidosFargateService")
                .serviceName("pedidos-fargate-service")
                .cluster(cluster)           // Required
                //Task Definition
                .cpu(512)                   // Default is 256
                .desiredCount(1)            // Default is 1
                .listenerPort(8080)         //Porta que LB escuta
                .assignPublicIp(true)       //Atribuindo IP Público ao container
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromEcrRepository(repository))
                                .containerPort(8080)
                                .containerName("pedidos-ms-db")
                                .environment(Map.of(
                                        "SPRING_DATASOURCE_URL", "jdbc:postgresql://" +
                                                Fn.importValue("pedidos-db-endpoint") + ":5432/pedidos-ms",
                                        "SPRING_DATASOURCE_USERNAME", "admin",
                                        "SPRING_DATASOURCE_PASSWORD", Fn.importValue("pedidos-db-password")
                                ))
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                                .logGroup(LogGroup.Builder.create(this, "PedidosMsLogGroup")
                                                        .logGroupName("PedidosMs")
                                                        .removalPolicy(RemovalPolicy.DESTROY)
                                                        .build())
                                                .streamPrefix("PedidosMs")
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

        //Expor a url do Load Balancer de Pedidos para que ocorra a comunicação com Pagamentos
        CfnOutput.Builder.create(this, "pedidos-load-balancer")
                .exportName("pedidos-load-balancer")
                .value(fargateService.getLoadBalancer().getLoadBalancerDnsName())
                .build();
    }
}

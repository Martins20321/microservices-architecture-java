package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.docdb.DatabaseCluster;
import software.amazon.awscdk.services.docdb.Login;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

public class DocumentDbStack extends Stack {
    public DocumentDbStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public DocumentDbStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(27017));

        CfnParameter dbPassword = CfnParameter.Builder.create(this, "dbPasswordDocdb")
                .type("String")
                .description("Senha do banco usando CFN Parameter")
                .noEcho(true)
                .build();

        DatabaseCluster cluster = DatabaseCluster.Builder.create(this, "Database")
                .masterUser(Login.builder()
                        .username("pagamentos_admin") // NOTE: 'admin' is reserved by DocumentDB
                        .password(SecretValue.unsafePlainText(dbPassword.getValueAsString()))
                        .build())
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .instances(1)  //Ele cria automaticamente uma instância primária (gravação e leitura)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(vpc.getPrivateSubnets())
                        .build())
                .copyTagsToSnapshot(true)
                .build();

        CfnOutput.Builder.create(this, "pagamentos-db-endpoint")
                .exportName("pagamentos-db-endpoint")
                .value(cluster.getClusterEndpoint().getHostname())
                .build();
    }
}

package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Collections;

public class RdsStack extends Stack {
    public RdsStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public RdsStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        CfnParameter dbPassword = CfnParameter.Builder.create(this, "DbPassword")
                .type("String")
                .description("Senha do banco usando CFN Parameter")
                .noEcho(true)
                .build();

        //Criando o Security Groups
        SecurityGroup sgRds = SecurityGroup.Builder.create(this, "RdsSecurityGroup")
                .securityGroupName("pedidos-ms-rds-sg")
                .description("Security Group para instancia RDS")
                .vpc(vpc)
                .build();
        sgRds.addIngressRule(Peer.anyIpv4(), Port.tcp(5432)); //Liberando qualquer requisição IPV4 na porta 5432(PostgreSQl)

        //Criando a instância do banco
        DatabaseInstance databaseInstance = DatabaseInstance.Builder.create(this, "microservices-db")
                .instanceIdentifier("pedidos-ms-db") //Nome da instância RDS
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_14_12)
                        .build()))
                .databaseName("pedidos_ms")
                .vpc(vpc)
                .credentials(Credentials.fromUsername("pedidos_admin", CredentialsFromUsernameOptions.builder()
                                .password(SecretValue.unsafePlainText(dbPassword.getValueAsString())) //Senha do CFN Parameter
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .multiAz(false)
                .allocatedStorage(10) //Alocação de GB de memória
                .securityGroups(Collections.singletonList(sgRds)) //Ele espera uma lista, mas estou passando somente um SG
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(vpc.getPrivateSubnets()) //Subnets privadas da nossa VPC
                        .build())
                .build();

        //Exportando valores para usar nas variáveis de ambiente do Service
        //Url de conexão do banco
        CfnOutput.Builder.create(this, "pedidos-db-endpoint")
                .exportName("pedidos-db-endpoint")
                .value(databaseInstance.getDbInstanceEndpointAddress())
                .build();

        CfnOutput.Builder.create(this, "pedidos-db-password")
                .exportName("pedidos-db-password")
                .value(dbPassword.getValueAsString())
                .build();
    }


}

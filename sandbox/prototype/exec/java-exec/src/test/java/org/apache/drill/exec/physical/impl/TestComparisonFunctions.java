package org.apache.drill.exec.physical.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.yammer.metrics.MetricRegistry;

import mockit.Injectable;
import mockit.NonStrictExpectations;

import org.apache.drill.common.config.DrillConfig;
import org.apache.drill.exec.expr.fn.FunctionImplementationRegistry;
import org.apache.drill.exec.memory.BufferAllocator;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.physical.PhysicalPlan;
import org.apache.drill.exec.physical.base.FragmentRoot;
import org.apache.drill.exec.planner.PhysicalPlanReader;
import org.apache.drill.exec.proto.CoordinationProtos;
import org.apache.drill.exec.proto.ExecProtos;
import org.apache.drill.exec.rpc.user.UserServer;
import org.apache.drill.exec.server.DrillbitContext;
import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestComparisonFunctions {
    static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestComparisonFunctions.class);

  DrillConfig c = DrillConfig.create();
    String COMPARISON_TEST_PHYSICAL_PLAN = "functions/comparisonTest.json";
  PhysicalPlanReader reader;
  FunctionImplementationRegistry registry;
  FragmentContext context;

  public void runTest(@Injectable final DrillbitContext bitContext,
                      @Injectable UserServer.UserClientConnection connection, String expression, int expectedResults) throws Throwable {

    new NonStrictExpectations(){{
      bitContext.getMetrics(); result = new MetricRegistry("test");
      bitContext.getAllocator(); result = BufferAllocator.getAllocator(c);
    }};

    String planString = Resources.toString(Resources.getResource(COMPARISON_TEST_PHYSICAL_PLAN), Charsets.UTF_8).replaceAll("EXPRESSION", expression);
    if(reader == null) reader = new PhysicalPlanReader(c, c.getMapper(), CoordinationProtos.DrillbitEndpoint.getDefaultInstance());
    if(registry == null) registry = new FunctionImplementationRegistry(c);
    if(context == null) context = new FragmentContext(bitContext, ExecProtos.FragmentHandle.getDefaultInstance(), connection, null, registry);
    PhysicalPlan plan = reader.readPhysicalPlan(planString);
    SimpleRootExec exec = new SimpleRootExec(ImplCreator.getExec(context, (FragmentRoot) plan.getSortedOperators(false).iterator().next()));

    while(exec.next()){
      assertEquals(String.format("Expression: %s;", expression), expectedResults, exec.getSelectionVector2().getCount());
    }

    if(context.getFailureCause() != null){
      throw context.getFailureCause();
    }

    assertTrue(!context.isFailed());
  }

  @Test
  public void testInt(@Injectable final DrillbitContext bitContext,
                           @Injectable UserServer.UserClientConnection connection) throws Throwable{
    runTest(bitContext, connection, "intColumn == intColumn", 100);
    runTest(bitContext, connection, "intColumn != intColumn", 0);
    runTest(bitContext, connection, "intColumn > intColumn", 0);
    runTest(bitContext, connection, "intColumn < intColumn", 0);
    runTest(bitContext, connection, "intColumn >= intColumn", 100);
    runTest(bitContext, connection, "intColumn <= intColumn", 100);
  }

  @Test
  public void testBigInt(@Injectable final DrillbitContext bitContext,
                      @Injectable UserServer.UserClientConnection connection) throws Throwable{
    runTest(bitContext, connection, "bigIntColumn == bigIntColumn", 100);
    runTest(bitContext, connection, "bigIntColumn != bigIntColumn", 0);
    runTest(bitContext, connection, "bigIntColumn > bigIntColumn", 0);
    runTest(bitContext, connection, "bigIntColumn < bigIntColumn", 0);
    runTest(bitContext, connection, "bigIntColumn >= bigIntColumn", 100);
    runTest(bitContext, connection, "bigIntColumn <= bigIntColumn", 100);
  }

  @Test
  public void testFloat4(@Injectable final DrillbitContext bitContext,
                         @Injectable UserServer.UserClientConnection connection) throws Throwable{
    runTest(bitContext, connection, "float4Column == float4Column", 100);
    runTest(bitContext, connection, "float4Column != float4Column", 0);
    runTest(bitContext, connection, "float4Column > float4Column", 0);
    runTest(bitContext, connection, "float4Column < float4Column", 0);
    runTest(bitContext, connection, "float4Column >= float4Column", 100);
    runTest(bitContext, connection, "float4Column <= float4Column", 100);
  }

  @Test
  public void testFloat8(@Injectable final DrillbitContext bitContext,
                         @Injectable UserServer.UserClientConnection connection) throws Throwable{
    runTest(bitContext, connection, "float8Column == float8Column", 100);
    runTest(bitContext, connection, "float8Column != float8Column", 0);
    runTest(bitContext, connection, "float8Column > float8Column", 0);
    runTest(bitContext, connection, "float8Column < float8Column", 0);
    runTest(bitContext, connection, "float8Column >= float8Column", 100);
    runTest(bitContext, connection, "float8Column <= float8Column", 100);
  }

  @Test
  public void testIntNullable(@Injectable final DrillbitContext bitContext,
                      @Injectable UserServer.UserClientConnection connection) throws Throwable{
    runTest(bitContext, connection, "intNullableColumn == intNullableColumn", 50);
    runTest(bitContext, connection, "intNullableColumn != intNullableColumn", 0);
    runTest(bitContext, connection, "intNullableColumn > intNullableColumn", 0);
    runTest(bitContext, connection, "intNullableColumn < intNullableColumn", 0);
    runTest(bitContext, connection, "intNullableColumn >= intNullableColumn", 50);
    runTest(bitContext, connection, "intNullableColumn <= intNullableColumn", 50);
  }
  @Test
  public void testBigIntNullable(@Injectable final DrillbitContext bitContext,
                         @Injectable UserServer.UserClientConnection connection) throws Throwable{
    runTest(bitContext, connection, "bigIntNullableColumn == bigIntNullableColumn", 50);
    runTest(bitContext, connection, "bigIntNullableColumn != bigIntNullableColumn", 0);
    runTest(bitContext, connection, "bigIntNullableColumn > bigIntNullableColumn", 0);
    runTest(bitContext, connection, "bigIntNullableColumn < bigIntNullableColumn", 0);
    runTest(bitContext, connection, "bigIntNullableColumn >= bigIntNullableColumn", 50);
    runTest(bitContext, connection, "bigIntNullableColumn <= bigIntNullableColumn", 50);
  }

    @AfterClass
    public static void tearDown() throws Exception{
        // pause to get logger to catch up.
        Thread.sleep(1000);
    }
}

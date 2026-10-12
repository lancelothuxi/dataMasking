package io.github.lancelot.datamasking;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * {@link MaskEngine} 注册语义测试：内置类型、自定义策略、注销与清空。
 */
public class MaskEngineRegisterTest {

    @Test
    public void testRegisterType() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        assertEquals(engine.process("mobile=13800138000"), "mobile=138******8000");
    }

    @Test
    public void testRegisterCustomStrategy() {
        MaskEngine engine = new MaskEngine();
        engine.register("token", value -> "***");
        assertEquals(engine.process("token=abc123"), "token=***");
    }

    @Test
    public void testCustomOverridesType() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        engine.register("mobile", value -> "x");
        assertEquals(engine.process("mobile=13800138000"), "mobile=x");
    }

    @Test
    public void testUnregister() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        engine.unregister("mobile");
        assertEquals(engine.process("mobile=13800138000"), "mobile=13800138000");
    }

    @Test
    public void testRegisterNullTypeUnregisters() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        engine.register("mobile", (MaskType) null);
        assertEquals(engine.process("mobile=13800138000"), "mobile=13800138000");
    }

    @Test
    public void testRegisterNullStrategyUnregisters() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        engine.register("mobile", (MaskStrategy) null);
        assertEquals(engine.process("mobile=13800138000"), "mobile=13800138000");
    }

    @Test
    public void testBlankOrNullFieldNameIgnored() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        engine.register(null, MaskType.MOBILE_PHONE);
        engine.register("  ", MaskType.CHINESE_NAME);
        // 已注册规则不受影响，也不抛异常
        assertEquals(engine.process("mobile=13800138000"), "mobile=138******8000");
    }

    @Test
    public void testClear() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        engine.register("name", MaskType.CHINESE_NAME);
        engine.clear();
        assertEquals(engine.process("mobile=13800138000 name=李四"), "mobile=13800138000 name=李四");
    }

    @Test
    public void testUnregisteredKeyPassThrough() {
        MaskEngine engine = new MaskEngine();
        engine.register("mobile", MaskType.MOBILE_PHONE);
        assertEquals(engine.process("nope=secret"), "nope=secret");
    }

    @Test
    public void testRegisterAllSpec() {
        MaskEngine engine = new MaskEngine();
        engine.registerAll("mobile:MOBILE_PHONE, name:CHINESE_NAME; password:ALL");
        assertEquals(engine.process("mobile=13800138000 name=李四 password=x"),
                "mobile=138******8000 name=李* password=***");
    }

    @Test
    public void testRegisterAllNullOrBlankIgnored() {
        MaskEngine engine = new MaskEngine();
        engine.registerAll(null);
        engine.registerAll("   ");
        assertEquals(engine.process("mobile=13800138000"), "mobile=13800138000");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRegisterAllUnknownTypeRejected() {
        new MaskEngine().registerAll("mobile:NOT_A_TYPE");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRegisterAllMalformedRejected() {
        new MaskEngine().registerAll("mobile");
    }
}
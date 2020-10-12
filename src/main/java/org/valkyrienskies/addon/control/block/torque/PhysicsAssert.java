package org.valkyrienskies.addon.control.block.torque;

public class PhysicsAssert {

    public static void assertPhysicsThread() {
        // This doesn't work anymore : (
        // assert Thread.currentThread() instanceof VSThread : "We are not running on a VW thread!";
    }
}

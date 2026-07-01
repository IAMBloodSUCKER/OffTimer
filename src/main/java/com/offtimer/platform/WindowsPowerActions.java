package com.offtimer.platform;

import com.offtimer.model.ActionType;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;

final class WindowsPowerActions {

    interface PowrProf extends com.sun.jna.Library {
        PowrProf INSTANCE = Native.load("PowrProf", PowrProf.class);

        boolean SetSuspendState(boolean hibernate, boolean forceCritical, boolean disableWakeEvent);
    }

    private WindowsPowerActions() {
    }

    static boolean tryExecute(ActionType action) {
        return switch (action) {
            case SHUTDOWN -> shutdown();
            case RESTART -> restart();
            case HIBERNATE -> PowrProf.INSTANCE.SetSuspendState(true, false, false);
            case SLEEP -> PowrProf.INSTANCE.SetSuspendState(false, false, false);
        };
    }

    private static boolean shutdown() {
        enableShutdownPrivilege();
        int flags = WinUser.EWX_SHUTDOWN | WinUser.EWX_POWEROFF | WinUser.EWX_HYBRID_SHUTDOWN;
        return User32.INSTANCE.ExitWindowsEx(flags, 0);
    }

    private static boolean restart() {
        enableShutdownPrivilege();
        return User32.INSTANCE.ExitWindowsEx(WinUser.EWX_REBOOT, 0);
    }

    private static void enableShutdownPrivilege() {
        WinNT.HANDLEByReference tokenHandle = new WinNT.HANDLEByReference();
        if (!Advapi32.INSTANCE.OpenProcessToken(
                Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_ADJUST_PRIVILEGES | WinNT.TOKEN_QUERY,
                tokenHandle)) {
            return;
        }
        try {
            WinNT.LUID luid = new WinNT.LUID();
            if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_SHUTDOWN_NAME, luid)) {
                return;
            }
            WinNT.TOKEN_PRIVILEGES privileges = new WinNT.TOKEN_PRIVILEGES(1);
            privileges.Privileges[0].Luid = luid;
            privileges.Privileges[0].Attributes = new WinDef.DWORD(WinNT.SE_PRIVILEGE_ENABLED);
            Advapi32.INSTANCE.AdjustTokenPrivileges(tokenHandle.getValue(), false, privileges, 0, null, null);
            Kernel32.INSTANCE.GetLastError();
        } finally {
            Kernel32.INSTANCE.CloseHandle(tokenHandle.getValue());
        }
    }
}

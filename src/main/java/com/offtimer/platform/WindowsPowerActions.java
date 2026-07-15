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
            case SHUTDOWN -> exitWindows(WinUser.EWX_SHUTDOWN | WinUser.EWX_POWEROFF | WinUser.EWX_FORCE);
            case RESTART -> exitWindows(WinUser.EWX_REBOOT | WinUser.EWX_FORCE);
            case HIBERNATE -> suspend(true);
            case SLEEP -> suspend(false);
        };
    }

    private static boolean exitWindows(int flags) {
        if (!enableShutdownPrivilege()) {
            System.err.println("OffTimer: shutdown privilege could not be enabled; trying Windows fallback");
            return false;
        }

        boolean success = User32.INSTANCE.ExitWindowsEx(
                new WinDef.UINT(flags),
                new WinDef.DWORD(0)).booleanValue();
        if (!success) {
            logWindowsError("ExitWindowsEx", Kernel32.INSTANCE.GetLastError());
        }
        return success;
    }

    private static boolean suspend(boolean hibernate) {
        boolean success = PowrProf.INSTANCE.SetSuspendState(hibernate, false, false);
        if (!success) {
            logWindowsError("SetSuspendState", Kernel32.INSTANCE.GetLastError());
        }
        return success;
    }

    private static boolean enableShutdownPrivilege() {
        WinNT.HANDLEByReference tokenHandle = new WinNT.HANDLEByReference();
        if (!Advapi32.INSTANCE.OpenProcessToken(
                Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_ADJUST_PRIVILEGES | WinNT.TOKEN_QUERY,
                tokenHandle)) {
            logWindowsError("OpenProcessToken", Kernel32.INSTANCE.GetLastError());
            return false;
        }

        try {
            WinNT.LUID luid = new WinNT.LUID();
            if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_SHUTDOWN_NAME, luid)) {
                logWindowsError("LookupPrivilegeValue", Kernel32.INSTANCE.GetLastError());
                return false;
            }

            WinNT.TOKEN_PRIVILEGES privileges = new WinNT.TOKEN_PRIVILEGES(1);
            privileges.Privileges[0].Luid = luid;
            privileges.Privileges[0].Attributes = new WinDef.DWORD(WinNT.SE_PRIVILEGE_ENABLED);

            Native.setLastError(WinError.ERROR_SUCCESS);
            if (!Advapi32.INSTANCE.AdjustTokenPrivileges(
                    tokenHandle.getValue(), false, privileges, 0, null, null)) {
                logWindowsError("AdjustTokenPrivileges", Kernel32.INSTANCE.GetLastError());
                return false;
            }

            int error = Kernel32.INSTANCE.GetLastError();
            if (error == WinError.ERROR_NOT_ALL_ASSIGNED) {
                logWindowsError("AdjustTokenPrivileges", error);
                return false;
            }
            return true;
        } finally {
            Kernel32.INSTANCE.CloseHandle(tokenHandle.getValue());
        }
    }

    private static void logWindowsError(String operation, int error) {
        System.err.println("OffTimer: " + operation + " failed with Windows error " + error);
    }
}

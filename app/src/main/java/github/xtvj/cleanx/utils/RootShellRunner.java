// SPDX-License-Identifier: GPL-3.0-or-later

package github.xtvj.cleanx.utils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.topjohnwu.superuser.Shell;

import java.io.InputStream;

class RootShellRunner extends Runner {
    @WorkerThread
    @NonNull
    @Override
    synchronized public Result runCommand() {
        Shell.Job shell = Shell.su(commands.toArray(new String[0]));
        for (InputStream is : inputStreams) {
            shell.add(is);
        }
        Shell.Result result = shell.exec();
        clear();
        return new Result(result.getOut(), result.getErr(), result.getCode());
    }
}

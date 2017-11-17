package github.tornaco.xposedmoduletest.ui.activity.ag;

import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.provider.AppGuardPackageProvider;
import github.tornaco.xposedmoduletest.ui.adapter.GuardAppListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.GuardAppPickerListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;

public class GuardAppPickerActivity extends GuardAppNavActivity {

    private boolean mShowSystemApp;

    @Override
    protected boolean isLockNeeded() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.app_list;
    }

    @Override
    protected void initView() {
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.initView();
        SwitchBar switchBar = (SwitchBar) findViewById(R.id.switchbar);
        switchBar.hide();
        fab.setImageResource(R.drawable.ic_check_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(GuardAppPickerActivity.this);
                p.setCancelable(false);
                p.setMessage("SAVING...");
                p.setIndeterminate(true);
                p.show();
                XExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<PackageInfo> packageInfoList = guardAppListAdapter.getPackageInfos();
                        github.tornaco.android.common.Collections.consumeRemaining(packageInfoList,
                                new Consumer<PackageInfo>() {
                                    @Override
                                    public void accept(PackageInfo packageInfo) {
                                        if (packageInfo.getGuard()) {
                                            AppGuardPackageProvider.insert(getApplicationContext(), packageInfo);
                                        }
                                    }
                                });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p.dismiss();
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadInstalled(mShowSystemApp);
    }

    @Override
    protected GuardAppListAdapter onCreateAdapter() {
        return new GuardAppPickerListAdapter(this);
    }

    @Override
    protected void setSummaryView() {
        TextView textView = (TextView) findViewById(R.id.summary);
        textView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picker, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.show_system_app).setChecked(mShowSystemApp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mShowSystemApp = !mShowSystemApp;
        invalidateOptionsMenu();
        startLoading();
        return super.onOptionsItemSelected(item);
    }
}
package ir.android.persiantask.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mohamadian.persianhorizontalexpcalendar.PersianHorizontalExpCalendar;
import com.mohamadian.persianhorizontalexpcalendar.enums.PersianCustomMarks;
import com.mohamadian.persianhorizontalexpcalendar.enums.PersianViewPagerType;
import com.mohamadian.persianhorizontalexpcalendar.model.CustomGradientDrawable;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ir.android.persiantask.R;
import ir.android.persiantask.data.db.entity.Tasks;
import ir.android.persiantask.data.db.factory.TasksViewModelFactory;
import ir.android.persiantask.ui.activity.task.AddEditTaskActivity;
import ir.android.persiantask.ui.adapters.TasksAdapter;
import ir.android.persiantask.utils.Init;
import ir.android.persiantask.viewmodels.TaskViewModel;
import kotlin.jvm.JvmStatic;

public class CalenderFragment extends Fragment {
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_BG_COLOR = "arg_bg_color";
    private static final String TAG = "TAG";
    private View inflater;
    private PersianHorizontalExpCalendar persianHorizontalExpCalendar;
    private RecyclerView taskRecyclerView;
    private FloatingActionButton addTaskBtn;
    private LinearLayout fab1, fab2;
    private CollapsingToolbarLayout toolBarLayout;
    public static final int ADD_TASK_REQUEST = 1;
    public static final int EDIT_TASK_REQUEST = 2;
    private TasksViewModelFactory factory;
    private TaskViewModel taskViewModel;
    private TasksAdapter tasksAdapter;
    private TextView taskText, reminderText;

    //boolean flag to know if main FAB is in open or closed state.
    private boolean fabExpanded = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calender_fragment, container, false);
        this.inflater = view;
        init();
        markDaysThatHaveTask();
        persianHorizontalExpCalendar = (PersianHorizontalExpCalendar) this.inflater.findViewById(R.id.persianCalendar);
        persianHorizontalExpCalendar.setTodayButtonTextSize(10);
        persianHorizontalExpCalendar.setPersianHorizontalExpCalListener(new PersianHorizontalExpCalendar.PersianHorizontalExpCalListener() {
            @Override
            public void onCalendarScroll(DateTime dateTime) {
                Log.i(TAG, "onCalendarScroll: " + dateTime.toString());
            }

            @Override
            public void onDateSelected(DateTime dateTime) {
                Log.i(TAG, "onDateSelected: " + dateTime.toString());
                /**
                 * update recycler view with select each day and show task that the chosen day is between start
                 * date and end date of it
                 */
                taskViewModel.getAllTasks().observe(CalenderFragment.this, new Observer<List<Tasks>>() {
                    @Override
                    public void onChanged(List<Tasks> tasks) {
                        List<Tasks> filteredTasks = new ArrayList<>();
                        for (Tasks task : tasks) {
                            if (Init.integerFormatFromStringDate(task.getTasks_startdate()) <= Init.integerFormatDate(dateTime) &&
                                    Init.integerFormatDate(dateTime) <= Init.integerFormatFromStringDate(task.getTasks_enddate())) {
                                filteredTasks.add(task);
                            }
                        }
                        tasksAdapter.submitList(filteredTasks);
                        taskRecyclerView.setAdapter(tasksAdapter);
                    }
                });
            }

            @Override
            public void onChangeViewPager(PersianViewPagerType persianViewPagerType) {
                Log.i(TAG, "onChangeViewPager: " + persianViewPagerType.name());
            }
        });

        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabExpanded == true){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });


        return view;
    }

    /**
     * mark days that have task(day that are between start date and end date of the task)
     */
    private void markDaysThatHaveTask() {
        taskViewModel.getAllTasks().observe(CalenderFragment.this, new Observer<List<Tasks>>() {
            @Override
            public void onChanged(List<Tasks> tasks) {
                for (Tasks task : tasks) {
                    for(int i = Init.integerFormatFromStringDate(task.getTasks_startdate()); i <= Init.integerFormatFromStringDate(task.getTasks_enddate()) ; i++){
                        markSomeDays(Init.convertIntegerToDateTime(i));
                    }
                }
            }
        });
    }

    private void init() {
        taskRecyclerView = this.inflater.findViewById(R.id.taskRecyclerView);
        addTaskBtn = this.inflater.findViewById(R.id.addTaskBtn);
        factory = new TasksViewModelFactory(getActivity().getApplication(), null);
        taskViewModel = ViewModelProviders.of(CalenderFragment.this, factory).get(TaskViewModel.class);
        tasksAdapter = new TasksAdapter(taskViewModel, getActivity(), getFragmentManager());
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fab1 =  this.inflater.findViewById(R.id.fab1);
        fab2 =  this.inflater.findViewById(R.id.fab2);
        taskText =  this.inflater.findViewById(R.id.taskText);
        reminderText =  this.inflater.findViewById(R.id.reminderText);

        //Only main FAB is visible in the beginning
        closeSubMenusFab();
    }


    @JvmStatic
    @NotNull
    public static CalenderFragment newInstance(@NotNull String title, int bgColorId) {
        return CalenderFragment.Companion.newInstance(title, bgColorId);
    }

    public static final class Companion {
        @JvmStatic
        @NotNull
        public static CalenderFragment newInstance(@NotNull String title, int bgColorId) {
            CalenderFragment calenderFragment = new CalenderFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ARG_TITLE, title);
            bundle.putInt(ARG_BG_COLOR, bgColorId);
            calenderFragment.setArguments(bundle);
            return calenderFragment;
        }

        private Companion() {
        }

    }

    public void markSomeDays(DateTime perChr) {
        this.persianHorizontalExpCalendar.markDate(new DateTime(perChr), PersianCustomMarks.SmallOval_Bottom, Color.RED).updateMarks();
    }

    public void cutomMarkTodaySelectedDay() {
        persianHorizontalExpCalendar
                .setMarkTodayCustomGradientDrawable(new CustomGradientDrawable(GradientDrawable.OVAL, new int[]{Color.parseColor("#55fefcea"), Color.parseColor("#55f1da36"), Color.parseColor("#55fefcea")})
                        .setstroke(2, Color.parseColor("#EFCF00"))
                        .setTextColor(Color.parseColor("#E88C02")))

                .setMarkSelectedDateCustomGradientDrawable(new CustomGradientDrawable(GradientDrawable.OVAL, new int[]{Color.parseColor("#55f3e2c7"), Color.parseColor("#55b68d4c"), Color.parseColor("#55e9d4b3")})
                        .setstroke(2, Color.parseColor("#E89314"))
                        .setTextColor(Color.parseColor("#E88C02")))
                .updateMarks();
    }

    private void closeSubMenusFab(){
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        taskText.setVisibility(View.GONE);
        reminderText.setVisibility(View.GONE);
        addTaskBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        fabExpanded=true;
        fab1.animate().translationY(-getResources().getDimension(R.dimen.fab_margin));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.fab_margin2));
        taskText.setVisibility(View.VISIBLE);
        reminderText.setVisibility(View.VISIBLE);
        addTaskBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_white_close));
    }
}

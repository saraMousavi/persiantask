package ir.android.taskroom.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mohamadian.persianhorizontalexpcalendar.PersianHorizontalExpCalendar;
import com.mohamadian.persianhorizontalexpcalendar.enums.PersianCustomMarks;
import com.mohamadian.persianhorizontalexpcalendar.enums.PersianViewPagerType;
import com.mohamadian.persianhorizontalexpcalendar.model.CustomGradientDrawable;

import org.joda.time.DateTime;
import org.joda.time.Days;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ir.android.taskroom.R;
import ir.android.taskroom.data.db.entity.Projects;
import ir.android.taskroom.data.db.entity.Reminders;
import ir.android.taskroom.data.db.entity.Subtasks;
import ir.android.taskroom.data.db.entity.Tasks;
import ir.android.taskroom.data.db.factory.ProjectsViewModelFactory;
import ir.android.taskroom.data.db.factory.SubTasksViewModelFactory;
import ir.android.taskroom.data.db.factory.TasksViewModelFactory;
import ir.android.taskroom.ui.activity.reminder.AddEditReminderActivity;
import ir.android.taskroom.ui.activity.task.AddEditTaskActivity;
import ir.android.taskroom.ui.adapters.ReminderAdapter;
import ir.android.taskroom.ui.adapters.TasksAdapter;
import ir.android.taskroom.utils.Init;
import ir.android.taskroom.viewmodels.ProjectViewModel;
import ir.android.taskroom.viewmodels.ReminderViewModel;
import ir.android.taskroom.viewmodels.SubTasksViewModel;
import ir.android.taskroom.viewmodels.TaskViewModel;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CalenderFragment extends Fragment {
    private static final String TAG = "TAG";
    private View inflater;
    private PersianHorizontalExpCalendar persianHorizontalExpCalendar;
    private RecyclerView recyclerView;
    private FloatingActionButton addTaskBtn;
    private LinearLayout fab1, fab2;
    private CollapsingToolbarLayout toolBarLayout;
    public static final int ADD_TASK_REQUEST = 1;
    public static final int EDIT_TASK_REQUEST = 2;
    public static final int ADD_REMINDER_REQUEST = 1;
    public static final int EDIT_REMINDER_REQUEST = 2;
    private TasksViewModelFactory factory;
    private TaskViewModel taskViewModel;
    private ReminderViewModel reminderViewModel;
    private TasksAdapter tasksAdapter;
    private ReminderAdapter reminderAdapter;
    private TextView taskText, reminderText;
    private Projects selectedProject;
    private SharedPreferences sharedPreferences;
    private ProjectsViewModelFactory projectFactory;
    private ProjectViewModel projectViewModel;
    private TextView taskList, reminderList;
    private DateTime clickedDateTime = null;

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
        onClickListener();
        onTouchListener();
        Init.initShowCaseView(getContext(), this.inflater.findViewById(R.id.persianCalendar),
                getString(R.string.seeListOfTaskAndReminderInCalender), "firstCalenderGuide", null);
        persianHorizontalExpCalendar = (PersianHorizontalExpCalendar) this.inflater.findViewById(R.id.persianCalendar);
        persianHorizontalExpCalendar.setTodayButtonTextSize(10);
        persianHorizontalExpCalendar.performClick();
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
                clickedDateTime = dateTime;
                if (taskList.getTag().equals("clicked")) {
                    initTaskRecyclerView();
                } else if (reminderList.getTag().equals("clicked")) {
                    initReminderRecyclerView();
                }

            }

            @Override
            public void onChangeViewPager(PersianViewPagerType persianViewPagerType) {
                Log.i(TAG, "onChangeViewPager: " + persianViewPagerType.name());
            }
        });

        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabExpanded == true) {
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });



        return view;
    }

    private void initTaskRecyclerView() {
        if (clickedDateTime != null) {
            taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), new Observer<List<Tasks>>() {
                @Override
                public void onChanged(List<Tasks> tasks) {
                    List<Tasks> filteredTasks = new ArrayList<>();
                    for (Tasks task : tasks) {
                        if (Init.integerFormatFromStringDate(task.getTasks_startdate()) <= Init.integerFormatDate(clickedDateTime) &&
                                Init.integerFormatDate(clickedDateTime) <= Init.integerFormatFromStringDate(task.getTasks_enddate())) {
                            filteredTasks.add(task);
                        }
                    }
                    tasksAdapter.submitList(filteredTasks);
                    recyclerView.setAdapter(tasksAdapter);
                }
            });
        }
    }

    private void initReminderRecyclerView() {
        if (clickedDateTime != null) {
            reminderViewModel.getAllReminders().observe(CalenderFragment.this, new Observer<List<Reminders>>() {

                @Override
                public void onChanged(List<Reminders> reminders) {
                    List<Tasks> filteredTasks = new ArrayList<>();
                    for (Reminders reminder : reminders) {
//                    if (Init.integerFormatFromStringDate(reminder.getTasks_startdate()) <= Init.integerFormatDate(clickedDateTime) &&
//                            Init.integerFormatDate(clickedDateTime) <= Init.integerFormatFromStringDate(task.getTasks_enddate())) {
//                        filteredTasks.add(task);
//                    }
                    }
                    reminderAdapter.submitList(reminders);
                    recyclerView.setAdapter(reminderAdapter);
                }
            });
        }
    }

    private void onTouchListener() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (taskList.getTag().equals("clicked")) {
                    Tasks selectedTask = tasksAdapter.getTaskAt(viewHolder.getAdapterPosition());
                    SubTasksViewModelFactory subfactory = new SubTasksViewModelFactory(getActivity().getApplication(), selectedTask.getTasks_id());
                    SubTasksViewModel subTasksViewModel = ViewModelProviders.of(getActivity(), subfactory).get(SubTasksViewModel.class);
                    subTasksViewModel.getAllSubtasks().observeForever(new Observer<List<Subtasks>>() {
                        @Override
                        public void onChanged(List<Subtasks> subtasks) {
                            for (Subtasks subtask : subtasks) {
                                subTasksViewModel.delete(subtask);
                            }
                            taskViewModel.delete(selectedTask);
                        }
                    });
                    Projects projects = selectedProject;
                    projects.setProjects_tasks_num(projects.getProjects_tasks_num() - 1);
                    projects.setProject_id(selectedProject.getProject_id());
                    //@TODO bellow function don't work
                    projectViewModel.update(projects);
                    persianHorizontalExpCalendar.updateMarks();

                    Snackbar
                            .make(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), getString(R.string.successDeleteTask), Snackbar.LENGTH_LONG)
                            .show();
                } else if (reminderList.getTag().equals("clicked")) {
                    Reminders selectedReminder = reminderAdapter.getReminderAt(viewHolder.getAdapterPosition());
                    reminderViewModel.delete(selectedReminder);

                    Snackbar
                            .make(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), getString(R.string.successDeleteReminder), Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void onClickListener() {
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddEditTaskActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST);
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
                startActivityForResult(intent, ADD_REMINDER_REQUEST);
            }
        });
        tasksAdapter.setOnItemClickListener(new TasksAdapter.TaskClickListener() {
            @Override
            public void switchContent(int subtaskConstarint, SubTaskFragment subTaskFragment) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(subtaskConstarint, subTaskFragment, subTaskFragment.toString());
                ft.addToBackStack(null);
                ft.commit();
            }

            @Override
            public void editTask(Tasks tasks) {
                Intent intent = new Intent(getActivity(), AddEditTaskActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("clickedTask", (Serializable) tasks);
                intent.putExtras(bundle);
                startActivityForResult(intent, EDIT_TASK_REQUEST);
            }
        });
        reminderAdapter.setOnItemClickListener(new ReminderAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Reminders reminders) {
                Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("clickedReminder", (Serializable) reminders);
                intent.putExtras(bundle);
                startActivityForResult(intent, EDIT_REMINDER_REQUEST);
            }
        });
        taskList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskList.setTag("clicked");
                Init.setBackgroundRightHeaderButton(getContext(), taskList);
                reminderList.setTag("unclicked");
                reminderList.setTextColor(getContext().getResources().getColor(R.color.black));
                reminderList.setBackground(getContext().getResources().getDrawable(R.drawable.unselected_left_corner_button_theme1));
                initTaskRecyclerView();
            }
        });
        reminderList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskList.setTag("unclicked");
                reminderList.setTag("clicked");
                taskList.setBackground(getContext().getResources().getDrawable(R.drawable.unselected_right_corner_button_theme1));
                taskList.setTextColor(getContext().getResources().getColor(R.color.black));
                Init.setBackgroundLeftHeaderButton(getContext(), reminderList);
                initReminderRecyclerView();
            }
        });
    }

    /**
     * mark days that have task(day that are between start date and end date of the task)
     */
    private void markDaysThatHaveTask() {
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), new Observer<List<Tasks>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChanged(List<Tasks> tasks) {
                for (Tasks task : tasks) {
                    DateTime dateTime1 = Init.convertIntegerToDateTime(Init.integerFormatFromStringDate(task.getTasks_startdate()));
                    if(!task.getTasks_enddate().isEmpty()){
                        DateTime dateTime2 = Init.convertIntegerToDateTime(Init.integerFormatFromStringDate(task.getTasks_enddate()));
                        int duration = Days.daysBetween(dateTime1, dateTime2).getDays();
                        for (int i = 0; i < duration; i++) {
                            markSomeDays(dateTime1.plusDays(i));
                        }
                    }
                }
            }
        });
    }

    private void init() {
        this.sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        Gson gson = new Gson();
        String projectJson = sharedPreferences.getString("selectedProject", "");
        selectedProject = gson.fromJson(projectJson, Projects.class);
        projectFactory = new ProjectsViewModelFactory(getActivity().getApplication(), selectedProject == null ? null : selectedProject.getProject_id());
        projectViewModel = ViewModelProviders.of(this, projectFactory).get(ProjectViewModel.class);

        recyclerView = this.inflater.findViewById(R.id.recyclerView);
        addTaskBtn = this.inflater.findViewById(R.id.addTaskBtn);
        reminderList = this.inflater.findViewById(R.id.reminderList);
        taskList = this.inflater.findViewById(R.id.taskList);
        Init.setBackgroundRightHeaderButton(getContext(), taskList);
        factory = new TasksViewModelFactory(getActivity().getApplication(), null);
        taskViewModel = ViewModelProviders.of(CalenderFragment.this, factory).get(TaskViewModel.class);
        reminderViewModel = ViewModelProviders.of(CalenderFragment.this).get(ReminderViewModel.class);
        tasksAdapter = new TasksAdapter(taskViewModel, getActivity(), getFragmentManager());
        reminderAdapter = new ReminderAdapter(getActivity(), reminderViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fab1 = this.inflater.findViewById(R.id.fab1);
        fab2 = this.inflater.findViewById(R.id.fab2);
        taskText = this.inflater.findViewById(R.id.taskText);
        reminderText = this.inflater.findViewById(R.id.reminderText);

        //Only main FAB is visible in the beginning
        closeSubMenusFab();
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

    private void closeSubMenusFab() {
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        taskText.setVisibility(View.GONE);
        reminderText.setVisibility(View.GONE);
        addTaskBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab() {
        fabExpanded = true;
        fab1.animate().translationY(-getResources().getDimension(R.dimen.fab_margin1));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.fab_margin2));
        taskText.setVisibility(View.VISIBLE);
        reminderText.setVisibility(View.VISIBLE);
        addTaskBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_white_close));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_REMINDER_REQUEST && resultCode == RESULT_OK) {
            Snackbar
                    .make(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), getString(R.string.successInsertReminder), Snackbar.LENGTH_LONG)
                    .show();
        } else if (requestCode == ADD_REMINDER_REQUEST && resultCode == RESULT_CANCELED) {
        }
        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK) {
            Projects projects = selectedProject;
            projects.setProjects_tasks_num(selectedProject.getProjects_tasks_num() + 1);
            projects.setProject_id(selectedProject.getProject_id());
            projectViewModel.update(projects);
            Snackbar
                    .make(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), getString(R.string.successInsertTask), Snackbar.LENGTH_LONG)
                    .show();
        } else if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_CANCELED) {
            Tasks tasks = new Tasks("", 0, 0, 0,
                    selectedProject.getProject_id(), "", 0, 0,
                    "", "", 0, "", "", false);
            tasks.setTasks_id(sharedPreferences.getLong("tempTaskID", 0));
            taskViewModel.delete(tasks);
        }
        closeSubMenusFab();
    }
}
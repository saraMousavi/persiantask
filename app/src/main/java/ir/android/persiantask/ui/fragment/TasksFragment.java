package ir.android.persiantask.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import ir.android.persiantask.R;
import ir.android.persiantask.data.db.entity.Projects;
import ir.android.persiantask.data.db.entity.Tasks;
import ir.android.persiantask.data.db.factory.ProjectsViewModelFactory;
import ir.android.persiantask.data.db.factory.TasksViewModelFactory;
import ir.android.persiantask.databinding.TasksFragmentBinding;
import ir.android.persiantask.ui.activity.task.AddEditTaskActivity;
import ir.android.persiantask.ui.adapters.TasksAdapter;
import ir.android.persiantask.viewmodels.ProjectViewModel;
import ir.android.persiantask.viewmodels.TaskViewModel;

import static android.app.Activity.RESULT_OK;

public class TasksFragment extends Fragment {

    public static final int ADD_TASK_REQUEST = 1;
    public static final int EDIT_TASK_REQUEST = 2;
    private TasksFragmentBinding tasksFragmentBinding;
    private TaskViewModel taskViewModel;
    private ProjectViewModel projectViewModel;
    private Integer selectedProjectedID;
    private View inflatedView;
    private RecyclerView taskRecyclerView;
    private TasksAdapter taskAdapter;
    private FloatingActionButton addTaskBtn;
    private Button firstAddTaskBtn;
    private TasksViewModelFactory factory;
    private ProjectsViewModelFactory projectFactory;
    private Integer tasksNum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tasksFragmentBinding = DataBindingUtil.inflate(
                inflater, R.layout.tasks_fragment, container, false);
        View view = tasksFragmentBinding.getRoot();
        this.inflatedView = view;
        init();

        tasksFragmentBinding.setTaskViewModel(taskViewModel);

        tasksRecyclerView();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        }).attachToRecyclerView(taskRecyclerView);

        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddEditTaskActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST);
            }
        });

        firstAddTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddEditTaskActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST);
            }
        });

        return view;
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("projectID")) {
                selectedProjectedID = bundle.getInt("projectID");
            }
        }
        factory = new TasksViewModelFactory(getActivity().getApplication(), selectedProjectedID);
        projectFactory = new ProjectsViewModelFactory(getActivity().getApplication(), selectedProjectedID);
        taskViewModel = ViewModelProviders.of(this, factory).get(TaskViewModel.class);
        projectViewModel = ViewModelProviders.of(this, projectFactory).get(ProjectViewModel.class);
        taskRecyclerView = this.inflatedView.findViewById(R.id.taskRecyclerView);
        firstAddTaskBtn = this.inflatedView.findViewById(R.id.firstAddTaskBtn);
        taskAdapter = new TasksAdapter(getActivity());
        addTaskBtn = this.inflatedView.findViewById(R.id.addTaskBtn);
    }


    /**
     * show tasks data depend on selected project in vertical recyclerview
     */
    private void tasksRecyclerView() {
        taskViewModel.getAllTasks().observe(this, new Observer<List<Tasks>>() {
            @Override
            public void onChanged(List<Tasks> tasks) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                View taskList = (View) inflatedView.findViewById(R.id.taskList);
                View taskEmptyList = (View) inflatedView.findViewById(R.id.taskEmptyList);
                tasksNum = tasks.size();
                if (tasksNum == 0) {
                    taskList.setVisibility(View.GONE);
                    taskEmptyList.setVisibility(View.VISIBLE);
                } else {
                    taskList.setVisibility(View.VISIBLE);
                    taskEmptyList.setVisibility(View.GONE);
                }
                taskAdapter.submitList(tasks);
            }
        });
        taskRecyclerView.setAdapter(taskAdapter);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskRecyclerView.setNestedScrollingEnabled(false);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK) {
            String taskName = data.getStringExtra(AddEditTaskActivity.EXTRA_NAME);
            Tasks tasks = new Tasks(1, 1, 1, selectedProjectedID, taskName, 1, 1, 1, 1, "", 1, 1, "");
            taskViewModel.insert(tasks);
            projectViewModel.getProjectsByID().observe(this, new Observer<Projects>() {
                @Override
                public void onChanged(Projects projects) {

                    projects.setProjects_tasks_num(tasksNum);
                    projectViewModel.update(projects);
                }
            });
            Snackbar
                    .make(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), getString(R.string.successInsertTask), Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}
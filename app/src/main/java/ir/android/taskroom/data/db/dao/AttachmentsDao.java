package ir.android.taskroom.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ir.android.taskroom.data.db.entity.Attachments;

@Dao
public interface AttachmentsDao {

    @Insert
    long insert(Attachments attachments);

    @Update
    void update(Attachments attachments);

    @Delete
    void delete(Attachments attachments);

    @Query("SELECT * FROM Attachments ORDER BY attachments_id DESC")
    LiveData<List<Attachments>> getAllAttachments();

    @Query("SELECT * FROM Attachments where tasks_id = :tasksID ORDER BY attachments_id DESC")
    LiveData<List<Attachments>> getAllTasksAttachments(Long tasksID);

    @Query("SELECT * FROM Attachments where reminders_id = :remindersID ORDER BY attachments_id DESC")
    LiveData<List<Attachments>> getAllRemindersAttachments(Long remindersID);

    @Query("SELECT * FROM Attachments where category_id = :categoryID ORDER BY attachments_id DESC")
    LiveData<List<Attachments>> getAllCategoryAttachments(Long categoryID);
}

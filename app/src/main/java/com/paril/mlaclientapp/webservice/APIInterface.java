package com.paril.mlaclientapp.webservice;

import com.paril.mlaclientapp.model.MLAAdminDetails;
import com.paril.mlaclientapp.model.MLAGradeTask;
import com.paril.mlaclientapp.model.MLAGroupKeys;
import com.paril.mlaclientapp.model.MLAGroupStatus;
import com.paril.mlaclientapp.model.MLAInstructorDetails;
import com.paril.mlaclientapp.model.MLAPosts;
import com.paril.mlaclientapp.model.MLARegisterUsers;
import com.paril.mlaclientapp.model.MLAScheduleDetailPostData;
import com.paril.mlaclientapp.model.MLAStudentDetails;
import com.paril.mlaclientapp.model.MLAStudentEnrollmentPostData;
import com.paril.mlaclientapp.model.MLASubjectDetails;
import com.paril.mlaclientapp.model.MLATaskDetails;
import com.paril.mlaclientapp.model.MLAUserGroups;
import com.paril.mlaclientapp.model.MLAUserPublicKeys;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("api/Register/GetRegisterAuth")
    Call<List<MLARegisterUsers>> authenticate(@Query("userName") String userName, @Query("password") String password);

    @GET("api/Admin/GetAdminByUserName")
    Call<List<MLAAdminDetails>> getAdminInfo(@Query("userName") String userName);


    @GET("api/Student/GetStudentByUserName")
    Call<List<MLAStudentDetails>> getStudentInfo(@Query("userName") String userName);


    @GET("api/Instructor/GetInstructorByUserName")
    Call<List<MLAInstructorDetails>> getInstInfo(@Query("userName") String userName);


    @GET("api/Admin/GetAllAdmin")
    Call<List<MLAAdminDetails>> getAdminUsers();

    @POST("api/DeleteAdmin/PostAdminRmv")
    Call<String> removeAdmin(@Query("idAdminRmv") String adminUserName);

    @POST("api/Register/PostAddAdmin")
    Call<MLAAdminDetails> addAdmin(@Query("adminUserName") String adminUserName, @Query("adminPassword") String adminPassword, @Query("adminFirsName") String adminFirsName, @Query("adminLastName") String adminLastName, @Query("adminTelephone") String adminTelephone, @Query("adminAddress") String adminAddress, @Query("adminAliasMailId") String adminAliasMailId, @Query("adminEmailId") String adminEmailId, @Query("adminSkypeId") String adminSkypeId);

    @POST("api/Admin/PostAdminUpdate")
    Call<String> updateAdmin(@Body MLAAdminDetails userDetails);


    @GET("api/Instructor/GetAllInstructor")
    Call<List<MLAInstructorDetails>> getInstructors();

    @POST("api/DeleteInstructor/PostInstructorRmv")
    Call<String> removeInstructor(@Query("idInstructorRmv") String idInstructorRmv);

    @POST("api/Register/PostAddInstructor")
    Call<MLAInstructorDetails> addInst(@Query("instUserName") String userName, @Query("instPassword") String password, @Query("instFirsName") String instFirsName, @Query("instLastName") String instLastName, @Query("instTelephone") String instTelephone, @Query("instAddress") String instAddress, @Query("instAliasMailId") String instAliasMailId, @Query("instEmailId") String instEmailId, @Query("instSkypeId") String instSkypeId);

    @POST("api/Instructor/PostInstructorUpdate/")
    Call<String> updateInstructor(@Body MLAInstructorDetails userDetails);


    @GET("api/Student/GetAllStudent")
    Call<List<MLAStudentDetails>> getStudents();

    @GET("api/Subject/GetSubjectByStudent")
    Call<ArrayList<MLASubjectDetails>> getSubjForStudent(@Query("idStudent") String idStudent);

    @POST("api/DeleteStudent/PostStudentRmv")
    Call<String> removeStudent(@Query("idStudentRmv") String idInstructorRmv);

    @POST("api/Register/PostAddStudent")
    Call<MLAStudentDetails> addStudent(@Query("userName") String userName, @Query("password") String password, @Query("firsName") String instFirsName, @Query("lastName") String instLastName, @Query("telephone") String instTelephone, @Query("address") String instAddress, @Query("aliasMailId") String instAliasMailId, @Query("emailId") String instEmailId, @Query("skypeId") String instSkypeId);

    @POST("api/Student/PostStudentUpdate/")
    Call<String> updateStudent(@Body MLAStudentDetails userDetails);

    @POST("api/Register/PostRegisterPassUpdate")
    Call<String> changePassword(@Query("userName") String userName, @Query("password") String password);


    @GET("api/Subject/GetAllSubject")
    Call<List<MLASubjectDetails>> getAllSubject();

    @GET("api/Subject/GetAllSubjectWithTask")
    Call<List<MLASubjectDetails>> getAllSubjectWithTask(@Query("flag") String subjectId);

    @POST("api/Subject/PostSubject")
    Call<MLASubjectDetails> addSubject(@Body MLASubjectDetails subjectDetails);

    @POST("api/SubjectUpdate/PostSubjectUpdate")
    Call<String> updateSubject(@Body MLASubjectDetails subjectDetails);


    @POST("api/SubjectRmv/PostSubjectRmv")
    Call<String> removeSubject(@Query("subject_id") String idSubject);

    @GET("api/DeEnrollStudent/GetDeEnrollBySubject")
    Call<List<MLAStudentDetails>> getDeEnrollBySub(@Query("idSubject") String subjectId);


    @GET("api/EnrollStudent/GetEnrollBySubject")
    Call<List<MLAStudentDetails>> getEnrollBySub(@Query("idSubject") String subjectId);

    @POST("api/EnrollStudent/PostEnrollStudent")
    Call<MLAStudentEnrollmentPostData> enrollBySub(@Body MLAStudentEnrollmentPostData listStudentData);


    @POST("api/DeEnrollStudent/PostDeEnroll")
    Call<MLAStudentEnrollmentPostData> deEnrollBySub(@Body MLAStudentEnrollmentPostData listStudentData);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @GET("api/EnrollStudent/GetSubjectByStd")
    Call<ArrayList<MLASubjectDetails>> getEnrolledSubjectForStudent(@Query("idStudent") String idStudent);

    @POST("api/Tasks/PostTask/")
    Call<String> addSchedule(@Body MLAScheduleDetailPostData details);


    @POST("api/ScheduleRmv/PostTaskRmv")
    Call<String> removeTasks(@Query("subject_id") String subjectId);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/Tasks/PostTaskUpdate")
    Call<String> updateTaskData(@Query("idTask") String idTask, @Query("topic") String topic, @Query("description") String desc);


    @GET("api/UserTasks/GetTasksByUser")
    Call<List<MLATaskDetails>> getTasksByUser(@Query("userId") String userName, @Query("userType") String userType);


    @GET("api/Tasks/GetTasksBySubject")
    Call<List<MLATaskDetails>> getTasksBySubject(@Query("subjectId") String subjectId);

    @GET("api/Tasks/GetStudentByTask")
    Call<List<MLAGradeTask>> getGrades(@Query("task") String task, @Query("subjectid") String subjId);

    @GET("api/Tasks/GetTasksByStudent")
    Call<List<MLATaskDetails>> getListTaskForStudent(@Query("subject") String subject, @Query("studentId") String studentId);


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/Tasks/PostGradeUpdate")
    Call<String> updateGrade(@Query("task_id") String taskId, @Query("student_id") String student_id, @Query("grade") String grade);

    @GET("api/Tasks/GetTasksByStudent")
    Call<List<MLAGradeTask>> getGradesForStudent(@Query("studentId") String studentId, @Query("subject") String subject);


    /***
     * Added by Alan Cleetus
     * Date: 4/15/2020
     */
    /**************************API's for  UserGroup*******************************/
    @GET("api/userGroup/GetAllUserGroups")
    Call<ArrayList<MLAUserGroups>> getAllUserGroups();

    @GET("api/userGroup/GetAllGroups")
    Call<ArrayList<MLAUserGroups>> getAllGroups();

    @GET("api/userGroup/GetGroupsByUserId")
    Call<ArrayList<MLAUserGroups>> getGroupsByUserId(@Query("userId") String userId);

    @GET("api/userGroup/GetUsersByGroupId")
    Call<ArrayList<MLAUserGroups>> getGroupsByGroupId(@Query("groupId") String groupId);

    @GET("api/userGroup/getGroupsByGroupName")
    Call<List<MLAUserGroups>> getGroupsByGroupName(@Query("groupName") String groupName);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/userGroup/CreateNewGroup")
    Call<Void> createNewGroup(@Query("userId") String userId, @Query("groupName") String groupName);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/userGroup/RmvUser")
    Call<Void> removeUserFromGroup(@Query("userId") String userId, @Query("groupId") String groupId);


    /**************************API's for  GroupKeys*******************************/
    @GET("api/GroupKeys/GetKey")
    Call<ArrayList<MLAGroupKeys>> getGroupKey(@Query("userId") String userId, @Query("groupId") String groupId, @Query("groupKeyVersion") String groupKeyVersion);

    @GET("api/GroupKeys/GetLatestKey")
    Call<ArrayList<MLAGroupKeys>> getLatestKey(@Query("userId") String userId,@Query("groupId") String groupId);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/GroupKeys/InsertNewGroupKey")
    Call<Void> addGroupKey(@Query("userId") int userId, @Query("groupId") int groupId, @Query("encryptedGroupKey") String encryptedGroupKey, @Query("groupKeyVersion") int groupKeyVersion);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/GroupKeys/UpdateStatus")
    Call<Void> updateGroupKeyStatus(@Query("groupId") String groupId, @Query("groupKeyVersion") String groupKeyVersion, @Query("status") String status);


    /**************************API's for  GroupStatus*******************************/
    @GET("api/GroupStatus/GetAll")
    Call<ArrayList<MLAGroupStatus>> getAllGroupStatus();

    @GET("api/GroupStatus/GetById")
    Call<ArrayList<MLAGroupStatus>> getGroupStatusById(@Query("groupId") String groupId);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/GroupStatus/PostStatus")
    Call<Void> addGroupStatus(@Query("groupId") String groupId, @Query("status") String status);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/GroupStatus/UpdateStatus")
    Call<Void> updateGroupStatus(@Query("groupId") String groupId, @Query("status") String status);

    /**************************API's for  Posts*******************************/
    @GET("api/posts/GetAllPosts")
    Call<ArrayList<MLAPosts>> getAllPosts();

    @GET("api/posts/GetPostsByGroup")
    Call<ArrayList<MLAPosts>> getPostsByGroup(@Query("userId") String userId);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/posts/NewPost")
    Call<Void> addPost(@Query("message") String message, @Query("messageKey") String messageKey, @Query("digitalSignature") String digitalSignature, @Query("signer") String signer, @Query("keyVersion") String keyVersion, @Query("groupId") String groupId, @Query("postType") String postType, @Query("originalPostId") String originalPostId);

    /**************************API's for  UserPublicKeys*******************************/
    @GET("api/UserPublicKeys/GetAll")
    Call<ArrayList<MLAUserPublicKeys>> GetAllUserPublicKeys();

    @GET("api/UserPublicKeys/GetById")
    Call<ArrayList<MLAUserPublicKeys>> getPublicKeyById(@Query("userId") String userId);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/UserPublicKeys/PostPublicKey")
    Call<Void> addPublicKey(@Query("userId") String userId, @Query("publicKey") String publicKey);


}
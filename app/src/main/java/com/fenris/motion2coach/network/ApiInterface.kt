package com.fenris.motion2coach.network

import com.fenris.motion2coach.model.*
import com.fenris.motion2coach.network.requests.*
import com.fenris.motion2coach.network.responses.*
import com.fenris.motion2coach.util.Constants
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {


    //    Step 1
    @POST(Constants.UPLOAD_UPLOAD_MEDIA_ONE)
    suspend fun uploadMediaOne(@Body body: SessionsRequestModel): Response<UploadResponse>


    //    Step 2
    @Headers("Content-Type: application/octet-stream")
    @PUT
    suspend fun actualUpload(@Url url: String, @Body image: RequestBody): Response<Void>

    //    Step 3
    @POST(Constants.ADD_VIDEO)
    suspend fun addUploadVideoToserver(@Body body: SessionsUploadRequestModel): Response<AddMediaResponse>

    @POST(Constants.ADD_ANNOTATIONS)
    suspend fun addAnnotations(@Body body: AddAnnotationRequestModel): Response<AddAnnotationsResponse>

    @POST(Constants.GET_ANNOTATIONS)
    suspend fun getAnnotations(@Body body: GetAnnotationRequestModel): Response<GetAnnotationResponseModel>

    @POST(Constants.DELETE_VIDEO)
    suspend fun deleteVideo(@Body body: DeleteVideoRequestModel): Response<DeleteVideoResponse>

    @POST(Constants.USER_SUBSCRIPTION)
    suspend fun getSubscriptionUrl(@Body body: SubscriptionUrlRequestModel): Response<SubscriptionUrlResponse>

    @POST(Constants.GET_ALLOWED_USERS)
    suspend fun getAllowedUsers(@Body body: CoachesRequestModel): Response<List<CoachesResponse>>

    @POST(Constants.ACCEPT_INVITATION)
    suspend fun acceptInvitation(@Body body: AcceptInvitationRequestModel): Response<AcceptInvitationResponse>

    @POST(Constants.SEND_COACH_INVITATION)
    suspend fun sendCoachInvitation(@Body body: InvitationRequestModel): Response<InvitationResponse>

    @POST(Constants.SEND_GUEST_INVITATION)
    suspend fun sendGuestInvitation(@Body body: InvitationRequestModel): Response<InvitationResponse>

    @POST(Constants.REMOVE_INVITATION)
    suspend fun removeInvitation(@Body body: RemoveInvitationRequestModel): Response<RemoveInvitationResponse>

    @POST(Constants.DELETE_USER)
    suspend fun deleteUser(@Body body: DeleteUserRequestModel): Response<DeleteUserResponse>

    @POST
    suspend fun getJsonReport(
        @Url url: String,
        @Body body: ReportRequestModel
    ): Response<List<ReportResponse>>


    @POST(Constants.ADD_SCAN)
    suspend fun addScanImage(@Body body: ScanUploadRequestModel): Response<AddMediaResponse>

    @POST(Constants.CITIES)
    suspend fun getCitiesByCountry(@Body body: CitiesRequestModel): Response<List<CitiesResponse>>

    @POST(Constants.GET_VIDEO)
    suspend fun getVideosFromServer(@Body body: VideosRequestModel): Response<List<VideoResponse>>

    @POST(Constants.GET_URL)
    suspend fun getUrl(@Body body: UrlRequestModel): Response<String>

    @POST(Constants.GET_URL)
    suspend fun getMultipleUrl(@Body body: UrlRequestModel): Response<List<String>>

    @GET(Constants.GET_PLAYER_TYPES)
    suspend fun getPlayerTypes(): Response<List<PlayerTypesResponse>>

    @POST(Constants.GET_ORIENTATIONS)
    suspend fun getOrientations(@Body body: UserIdRequestModel): Response<List<OrientationResponse>>

    @POST
    suspend fun getPositions(
        @Url url: String,
        @Body body: ReportRequestModel
    ): Response<PositionsResponse>

    @POST
    suspend fun getHighlightsTwo(
        @Url url: String,
        @Body body: ReportRequestModel
    ): Response<HighlightsTwoResponse>

    @POST
    suspend fun getKynamaticReport(
        @Url url: String,
        @Body body: ReportRequestModel
    ): Response<KinamaticReportResponse>

    @POST
    suspend fun updatePositions(
        @Url url: String,
        @Body body: UpdatePositionsRequestModel
    ): Response<UpdatePositionsResponse>

    @POST(Constants.GET_USER)
    suspend fun getUserById(@Body body: ProfileUserRequestModel): Response<UserResponse>

    @POST(Constants.UPDATE_USER)
    suspend fun updateUser(@Body body: ProfileUserRequestModel): Response<UserResponse>

    @POST(Constants.CHANGE_PASSWORD)
    suspend fun changePassword(@Body body: ChangePasswordRequestModel): Response<UserResponse>

    @POST(Constants.FORGOT_PASSWORD)
    suspend fun forgotPassword(@Body body: ForgotPassRequestModel): Response<ForgotPassResponse>

    @POST(Constants.EMAIL_VALID)
    suspend fun emailValid(@Body body: EmailValidRequestModel): Response<EmailValidResponse>

    @GET(Constants.COUNTRIES)
    suspend fun getCountries(): Response<List<CountriesResponse>>

    @GET(Constants.GENDER)
    suspend fun getGenders(): Response<List<GendersResponse>>

    @GET(Constants.MASTER_DATA)
    suspend fun getMasterData(): Response<MasterDataResponse>

    @GET(Constants.ROLES)
    suspend fun getRoles(): Response<List<RolesResponse>>

    @POST(Constants.LOGIN)
    suspend fun loginUser(@Body body: LoginRequestModel): Response<LoginResponse>

    @POST(Constants.ACTIVATE)
    suspend fun activateUser(@Body body: ActivateUserRequestModel): Response<ActiveUserResponse>

    @POST(Constants.RESEND_OTP)
    suspend fun resendOtp(@Body body: ResendOtpRequestModel): Response<ActiveUserResponse>

    @POST(Constants.REGISTER)
    suspend fun registerUser(@Body body: RegisterRequestModel): Response<RegisterResponse>

    @Headers("Accept: application/json")
    @GET
    suspend fun getVideoReport(@Url url: String): Response<VideoReportResponse>


}
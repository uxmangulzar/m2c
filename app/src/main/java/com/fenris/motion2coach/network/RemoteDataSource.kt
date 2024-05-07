package com.fenris.motion2coach.network

import com.fenris.motion2coach.network.requests.*
import okhttp3.RequestBody
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiInterface: ApiInterface) {
    suspend fun uploadMediaOne(body: SessionsRequestModel) =
        apiInterface.uploadMediaOne(body)

    suspend fun getJsonReport(url: String, body: ReportRequestModel) =
        apiInterface.getJsonReport(url, body)

    suspend fun getPositions(url: String, body: ReportRequestModel) =
        apiInterface.getPositions(url, body)

    suspend fun getHighlightsTwo(url: String, body: ReportRequestModel) =
        apiInterface.getHighlightsTwo(url, body)

    suspend fun getKynamaticReport(url: String, body: ReportRequestModel) =
        apiInterface.getKynamaticReport(url, body)

    suspend fun updatePositions(url: String, body: UpdatePositionsRequestModel) =
        apiInterface.updatePositions(url, body)


    suspend fun addVideo(body: SessionsUploadRequestModel) =
        apiInterface.addUploadVideoToserver(body)

    suspend fun getVideosFromServer(body: VideosRequestModel) =
        apiInterface.getVideosFromServer(body)

    suspend fun deleteVideo(body: DeleteVideoRequestModel) =
        apiInterface.deleteVideo(body)

    suspend fun getSubscriptionUrl(body: SubscriptionUrlRequestModel) =
        apiInterface.getSubscriptionUrl(body)

    suspend fun getAllowedUsers(body: CoachesRequestModel) =
        apiInterface.getAllowedUsers(body)

    suspend fun acceptInvitation(body: AcceptInvitationRequestModel) =
        apiInterface.acceptInvitation(body)

    suspend fun sendCoachInvitation(body: InvitationRequestModel) =
        apiInterface.sendCoachInvitation(body)

    suspend fun sendGuestInvitation(body: InvitationRequestModel) =
        apiInterface.sendGuestInvitation(body)

    suspend fun removeInvitation(body: RemoveInvitationRequestModel) =
        apiInterface.removeInvitation(body)

    suspend fun deleteUser(body: DeleteUserRequestModel) =
        apiInterface.deleteUser(body)

    suspend fun addScan(body: ScanUploadRequestModel) =
        apiInterface.addScanImage(body)

    suspend fun getUrl(body: UrlRequestModel) =
        apiInterface.getUrl(body)

    suspend fun getMultipleUrl(body: UrlRequestModel) =
        apiInterface.getMultipleUrl(body)
    suspend fun addAnnotations(body: AddAnnotationRequestModel) =
        apiInterface.addAnnotations(body)

    suspend fun getAnnotations(body: GetAnnotationRequestModel) =
        apiInterface.getAnnotations(body)

    suspend fun getCountries() =
        apiInterface.getCountries()

    suspend fun getGenders() =
        apiInterface.getGenders()

    suspend fun getMasterData() =
        apiInterface.getMasterData()

    suspend fun getRoles() =
        apiInterface.getRoles()

    suspend fun loginUser(body: LoginRequestModel) =
        apiInterface.loginUser(body)

    suspend fun activateUser(body: ActivateUserRequestModel) =
        apiInterface.activateUser(body)

    suspend fun resendOtp(body: ResendOtpRequestModel) =
        apiInterface.resendOtp(body)

    suspend fun registerUser(body: RegisterRequestModel) =
        apiInterface.registerUser(body)

    suspend fun getVideoReport(body: String) =
        apiInterface.getVideoReport(body)

    suspend fun getCities(body: CitiesRequestModel) =
        apiInterface.getCitiesByCountry(body)

    suspend fun getOrientations(body: UserIdRequestModel) =
        apiInterface.getOrientations(body)

    suspend fun getPlayerTypes() =
        apiInterface.getPlayerTypes()

    suspend fun getUserProfile(body: ProfileUserRequestModel) =
        apiInterface.getUserById(body)

    suspend fun updateUser(body: ProfileUserRequestModel) =
        apiInterface.updateUser(body)

    suspend fun changePassword(body: ChangePasswordRequestModel) =
        apiInterface.changePassword(body)

    suspend fun forgotPassword(body: ForgotPassRequestModel) =
        apiInterface.forgotPassword(body)

    suspend fun emailValid(body: EmailValidRequestModel) =
        apiInterface.emailValid(body)

    suspend fun actualUpload(url: String, image: RequestBody) =
        apiInterface.actualUpload(url, image)
}
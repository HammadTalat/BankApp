import { httpClient } from "../../../../api/httpClient.js";

export function getPendingUsers() {
    return httpClient.get("/api/v1/admin/users?approvalStatus=PENDING");
}
export function ApproveUser(userId){


    return httpClient.post(`/api/v1/admin/users/${userId}/approve`);
}
export function RejectUser(userId){


    return httpClient.post(`/api/v1/admin/users/${userId}/reject`);
}


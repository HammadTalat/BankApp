import { useState , useEffect} from "react";
import { UsersRound } from "lucide-react";

import Alert from "../../../../shared/components/feedback/Alert";
import EmptyState from "../../../../shared/components/feedback/EmptyState";
import PageHeader from "../../../../shared/components/ui/PageHeader";
import TableContainer from "../../../../shared/components/ui/TableContainer";
import PendingUsersTable from "../components/PendingUsersTable";
import UserReviewModal from "../components/UserReviewModal";
import {getPendingUsers} from "../api/adminUsersApi.js"
import {ApproveUser, RejectUser} from "../api/adminUsersApi.js"

function PendingUsersPage() {

    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(()=>{
        async  function loadPendingUsers(){
            const response = await  getPendingUsers();
            setUsers(response);
            setLoading(false);
            console.log(response);
        }
        loadPendingUsers();
    },[])




    const [review, setReview] = useState({
        action: null,
        user: null,
    });
    const [feedback, setFeedback] = useState(null);

    function openReview(action, user) {
        setReview({
            action,
            user,
        });
    }

    function closeReview() {
        setReview({
            action: null,
            user: null,
        });
    }

   async function handleConfirmReview() {
        if (!review.user || !review.action) {
            return;
        }

        const reviewedUser = review.user;
        const wasApproved = review.action === "approve";
        if(wasApproved){
            const response = await ApproveUser(reviewedUser.id);
            console.log(response);

        }
        else{
            const response = await RejectUser(reviewedUser.id);
            console.log(response);
        }

        setUsers((currentUsers) =>
            currentUsers.filter((user) => user.id !== reviewedUser.id),
        );
        setFeedback({
            title: wasApproved
                ? "User approved"
                : "Application rejected",
            message: wasApproved
                ? `${reviewedUser.name} was approved and a new bank account was created.`
                : `${reviewedUser.name}'s application was rejected.`,
        });
        closeReview();
    }
    if (loading) {
        return (
            <p className="p-6">
                Loading dashboard...
            </p>
        );
    }
    return (
        <section className="space-y-6">
            <PageHeader
                title="Pending Users"
                description="Review applications and create bank accounts through approval."
            />

            {feedback && (
                <Alert
                    type="success"
                    title={feedback.title}
                >
                    {feedback.message}
                </Alert>
            )}

            {users.length > 0 ? (
                <PendingUsersTable
                    users={users}
                    onApprove={(user) => openReview("approve", user)}
                    onReject={(user) => openReview("reject", user)}
                />
            ) : (
                <TableContainer className="min-h-[32rem] shadow-none">
                    <div className="flex min-h-[32rem] items-center justify-center">
                        <EmptyState
                            icon={UsersRound}
                            title="No pending users"
                            description="There are currently no account-holder applications waiting for review."
                        />
                    </div>
                </TableContainer>
            )}

            <UserReviewModal
                isOpen={Boolean(review.user)}
                user={review.user}
                action={review.action}
                onClose={closeReview}
                onConfirm={handleConfirmReview}
            />
        </section>
    );
}

export default PendingUsersPage;

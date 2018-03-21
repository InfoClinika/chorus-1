var Messages = {
    NO_MEMBERS:"No Members",
    MEMBER:"Member",
    MEMBERS:"Members",
    NO_PROJECTS:"No projects",
    PROJECT:"project",
    PROJECTS:"projects"
};

function getCountOfMembers (memberCount) {
    if (memberCount == 0) {
        return Messages.NO_MEMBERS;
    } else if (memberCount == 1) {
        return memberCount + " " + Messages.MEMBER;
    } else {
        return memberCount + " " + Messages.MEMBERS;
    }
}

var DragNDropMessages = {
    UNSUPPORTED_FILES_FILTERED:"Unsupported files filtered",
    DUPLICATES_REMOVED:"Duplicates removed",
    EMPTY_FOLDERS_FILTERED: "Empty folders filtered"
};
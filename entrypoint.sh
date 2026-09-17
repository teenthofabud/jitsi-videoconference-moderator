#!/bin/sh
set -e

exec java -cp app:app/lib/* com.teenthofabud.videoconference.mdr.JitsiConferenceModeratorApplication "$@"

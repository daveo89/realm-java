/*
 * Copyright 2017 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;

/**
 * Class used to encapsulate progress notifications when either downloading or uploading Realm data.
 * Each instance of this class is an immutable snapshot of the current progress.
 * <p>
 * If the {@link ProgressListener} was registered with {@link ProgressMode#INDEFINETELY}, the progress reported by
 * {@link #getFractionTransferred()} can both increase and decrease since more changes might be added while
 * the progres listener is registered. This means it is possible for one notification to report
 * {@code true} for {@link #isTransferComplete()}, and then on the next event report {@code false}.
 * <p>
 * if the {@link ProgressListener} was registered with {@link ProgressMode#CURRENT_CHANGES}, progress can only ever
 * increase, and once {@link #isTransferComplete()} returns {@code true}, no further events will be generated.
 *
 * @see SyncSession#addDownloadProgressListener(ProgressMode, ProgressListener)
 * @see SyncSession#addUploadProgressListener(ProgressMode, ProgressListener)
 */
public class Progress {

    private final long transferredBytes;
    private final long transferableBytes;

    /**
     * Create a snapshot of the current progress when downloading or uploading changes.
     *
     * @param transferredBytes number of bytes transfered.
     * @param transferableBytes total number of bytes that needs to be transferred (including those already transferred).
     */
    Progress(long transferredBytes, long transferableBytes) {
        this.transferredBytes = transferredBytes;
        this.transferableBytes = transferableBytes;
    }

    /**
     * Returns the total number of bytes that has been transferred since the {@link ProgressListener} was added.
     *
     * @return the total number of bytes transferred since the {@link ProgressListener} was added.
     */
    public long getTransferredBytes() {
        return transferredBytes;
    }

    /**
     * Returns the total number of transferable bytes (bytes that have been transferred + bytes pending transfer).
     * <p>
     * If the {@link ProgressListener} is tracking downloads, this number represents the size of the changesets
     * generated by all other clients using the Realm.
     * <p>
     * If the {@link ProgressListener} is tracking uploads, this number represents the size of changesets created
     * locally.
     *
     * @return the total number of bytes that has been transferred + number of bytes still pending transfer.
     */
    public long getTransferableBytes() {
        return transferableBytes;
    }

    /**
     * The fraction of bytes transferred out of all transferable bytes. Counting from since the {@link ProgressListener}
     * was added.
     *
     * @return a number between {@code 0.0} and {@code 1.0}, where {@code 0.0} represents that no data has been
     *         transferred yet, and {@code 1.0} that all data has been transferred.
     */
    public double getFractionTransferred() {
        double percentage = (double) transferredBytes / (double) transferableBytes;
        return percentage > 1.0D ? 1.0D : percentage;
    }

    /**
     * Returns true when all pending bytes have been transferred.
     * <p>
     * If the {@link ProgressListener} was registered with {@link ProgressMode#INDEFINETELY}, this method can return
     * {@code false} for subsequent events after returning {@code true}.
     * <p>
     * If the {@link ProgressListener} was registered with {@link ProgressMode#CURRENT_CHANGES}, when this method
     * returns {@code true}, no more progress events will be sent.
     *
     * @return {@code true} if all changes have been transferred, {@code false} otherwise.
     */
    public boolean isTransferComplete() {
        return transferredBytes >= transferableBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Progress progress = (Progress) o;

        if (transferredBytes != progress.transferredBytes) return false;
        return transferableBytes == progress.transferableBytes;

    }

    @Override
    public int hashCode() {
        int result = (int) (transferredBytes ^ (transferredBytes >>> 32));
        result = 31 * result + (int) (transferableBytes ^ (transferableBytes >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Progress{" +
                "transferredBytes=" + transferredBytes +
                ", transferableBytes=" + transferableBytes +
                '}';
    }
}

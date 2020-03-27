package com.iexec.resultproxy.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.iexec.common.chain.ChainDeal;
import com.iexec.common.chain.ChainTask;
import com.iexec.common.task.TaskDescription;
import com.iexec.common.utils.BytesUtils;
import com.iexec.resultproxy.chain.IexecHubService;
import com.iexec.resultproxy.ipfs.IpfsResultService;
import com.iexec.resultproxy.mongo.MongoResultService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProxyServiceTest {

    @Mock
    private IexecHubService iexecHubService;

    @Mock
    private MongoResultService mongoResultService;

    @Mock
    private IpfsResultService ipfsResultService;


    @InjectMocks
    private ProxyService proxyService;

    private Integer chainId;
    private String chainDealId;
    private String chainTaskId;
    private String walletAddress;
    private byte[] zip;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        chainId = 17;
        chainDealId = "Oxdea1";
        chainTaskId = "0x1";
        walletAddress = "0x123abc";
        zip = new byte[10];
    }

    @Test
    public void isNotAbleToUploadSinceResultAlreadyExistsWithMongo() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(false);
        when(mongoResultService.doesResultExist(chainTaskId)).thenReturn(true);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress, zip)).isFalse();
    }

    @Test
    public void isNotAbleToUploadSinceResultAlreadyExistsWithIpfs() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(true);
        when(ipfsResultService.doesResultExist(chainTaskId)).thenReturn(true);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress, zip)).isFalse();
    }

    @Test
    public void isNotAbleToUploadSinceChainStatusIsNotRevealedWithMongo() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(false);
        when(mongoResultService.doesResultExist(chainTaskId)).thenReturn(true);
        when(iexecHubService.isStatusTrueOnChain(any(), any(), any())).thenReturn(false);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress, zip)).isFalse();
    }

    @Test
    public void isNotAbleToUploadSinceChainStatusIsNotRevealedWithIpfs() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(true);
        when(ipfsResultService.doesResultExist(chainTaskId)).thenReturn(true);
        when(iexecHubService.isStatusTrueOnChain(any(), any(), any())).thenReturn(false);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress, zip)).isFalse();
    }

    @Test
    public void isAbleToUploadWithMongo() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(false);
        when(mongoResultService.doesResultExist(chainTaskId)).thenReturn(false);
        when(iexecHubService.isStatusTrueOnChain(any(), any(), any())).thenReturn(true);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress, zip)).isTrue();
    }

    //@Test
    public void isAbleToUploadWithIpfs() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(true);
        when(ipfsResultService.doesResultExist(chainTaskId)).thenReturn(false);
        when(iexecHubService.isStatusTrueOnChain(any(), any(), any())).thenReturn(true);

        assertThat(proxyService.canUploadResult(chainTaskId, walletAddress, zip)).isTrue();
    }

    @Test
    public void isNotAuthorizedToGetResultSinceWalletAddressDifferentFromRequester() {
        String requester = "0xa";
        String beneficiary = BytesUtils.EMPTY_ADDRESS;
        when(iexecHubService.getChainTask("0x1")).thenReturn(Optional.of(ChainTask.builder().dealid(chainDealId).build()));
        when(iexecHubService.getChainDeal(chainDealId)).thenReturn(Optional.of(ChainDeal.builder().requester(requester).beneficiary(beneficiary).build()));
        assertThat(proxyService.isOwnerOfResult(chainId, chainTaskId, "0xabcd1339Ec7e762e639f4887E2bFe5EE8023E23E")).isFalse();
    }

    @Test
    public void isNotAuthorizedToGetResultSinceCannotGetChainTask() {
        when(iexecHubService.getChainTask("0x1")).thenReturn(Optional.empty());

        assertThat(proxyService.isOwnerOfResult(chainId, chainTaskId, "0xabcd1339Ec7e762e639f4887E2bFe5EE8023E23E")).isFalse();
    }

    @Test
    public void isNotAuthorizedToGetResultSinceCannotGetChainDeal() {
        when(iexecHubService.getChainTask("0x1")).thenReturn(Optional.of(ChainTask.builder().dealid(chainDealId).build()));
        when(iexecHubService.getChainDeal(chainDealId)).thenReturn(Optional.empty());

        assertThat(proxyService.isOwnerOfResult(chainId, chainTaskId, "0xabcd1339Ec7e762e639f4887E2bFe5EE8023E23E")).isFalse();
    }

    @Test
    public void isNotOwnerOfResultSinceWalletAddressDifferentFromBeneficiary() {
        String beneficiary = "0xb";
        when(iexecHubService.getChainTask("0x1")).thenReturn(Optional.of(ChainTask.builder().dealid(chainDealId).build()));
        when(iexecHubService.getChainDeal(chainDealId)).thenReturn(Optional.of(ChainDeal.builder().beneficiary(beneficiary).build()));
        assertThat(proxyService.isOwnerOfResult(chainId, chainTaskId, "0xabcd1339Ec7e762e639f4887E2bFe5EE8023E23E")).isFalse();
    }

    @Test
    public void isNotOwnerOfResultSinceWalletAddressShouldBeBeneficiary() {
        String beneficiary = "0xb";
        when(iexecHubService.getChainTask("0x1")).thenReturn(Optional.of(ChainTask.builder().dealid(chainDealId).build()));
        when(iexecHubService.getChainDeal(chainDealId)).thenReturn(Optional.of(ChainDeal.builder().beneficiary(beneficiary).build()));
        assertThat(proxyService.isOwnerOfResult(chainId, chainTaskId,"0xabcd1339Ec7e762e639f4887E2bFe5EE8023E23E")).isFalse();
    }

    @Test
    public void isOwnerOfResultNonTeeTask() {
        String beneficiary = "0xbeneficiary";
        String requester = "0xrequester";
        TaskDescription taskDescription = TaskDescription.builder()
                .requester(requester)
                .beneficiary(beneficiary)
                .isTeeTask(false)
                .build();
        when(iexecHubService.getTaskDescriptionFromChain(chainTaskId)).thenReturn(Optional.of(taskDescription));

        assertThat(proxyService.isOwnerOfResult(chainId, chainTaskId, "0xbeneficiary")).isTrue();
    }

    @Test
    public void isOwnerOfResultTeeTask() {
        String beneficiary = "0xbeneficiary";
        String requester = "0xrequester";
        TaskDescription taskDescription = TaskDescription.builder()
                .requester(requester)
                .beneficiary(beneficiary)
                .isTeeTask(true)
                .build();
        when(iexecHubService.getTaskDescriptionFromChain(chainTaskId)).thenReturn(Optional.of(taskDescription));

        assertThat(proxyService.isOwnerOfResult(chainId, chainTaskId, "0xrequester")).isTrue();
    }

    @Test
    public void isPublicResult() {
        when(iexecHubService.isPublicResult(chainTaskId, 0)).thenReturn(true);
        assertThat(proxyService.isPublicResult(chainTaskId)).isTrue();
    }

    @Test
    public void isNotPublicResult() {
        String beneficiary = "0xb";
        when(iexecHubService.getTaskBeneficiary(chainTaskId, chainId)).thenReturn(Optional.of(beneficiary));
        assertThat(proxyService.isPublicResult(chainTaskId)).isFalse();
    }
}
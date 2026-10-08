package com.example.delivery.store.service;

import com.example.delivery.common.exception.NotFoundException;
import com.example.delivery.common.exception.StoreAlreadyExistsException;
import com.example.delivery.member.entity.Member;
import com.example.delivery.member.repository.MemberRepository;
import com.example.delivery.store.dto.StoreRequest;
import com.example.delivery.store.dto.StoreResponse;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public StoreResponse createStore(Long memberId, StoreRequest request) {
        // 사장님 한 명당 가게 하나. DB에도 owner_id UNIQUE 제약이 있다.
        if (storeRepository.existsByOwner_Id(memberId)) {
            throw new StoreAlreadyExistsException();
        }
        Member owner = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));

        return StoreResponse.from(storeRepository.save(new Store(owner, request.name())));
    }

    public StoreResponse getMyStore(Long memberId) {
        return storeRepository.findByOwner_Id(memberId)
                .map(StoreResponse::from)
                .orElseThrow(() -> new NotFoundException("등록된 가게가 없습니다."));
    }
}
